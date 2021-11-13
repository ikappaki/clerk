;; # Hashing Things!!!!
(ns ^:nextjournal.clerk/no-cache nextjournal.clerk.hashing
  (:refer-clojure :exclude [hash read-string])
  (:require [babashka.fs :as fs]
            [clojure.core :as core]
            [clojure.java.classpath :as cp]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.analyzer.jvm :as ana]
            [clojure.tools.analyzer.passes.jvm.emit-form :as ana.passes.ef]
            [edamame.core :as edamame]
            [multihash.core :as multihash]
            [multihash.digest :as digest]
            [rewrite-clj.node :as n]
            [rewrite-clj.parser :as p]
            [weavejester.dependency :as dep]))

(defn var-name
  "Takes an analyzed `form` and returns the name of the var, if it exists."
  [form]
  (when (and (sequential? form)
             (= 'def (first form)))
    (second form)))

#_(var-name '(def foo :bar))

(defn no-cache? [form]
  (or (-> (if-let [vn (var-name form)] vn form) meta :nextjournal.clerk/no-cache boolean)
      (-> *ns* meta :nextjournal.clerk/no-cache boolean)))

#_(no-cache? '(rand-int 10))

(defn sha1-base58 [s]
  (-> s digest/sha1 multihash/base58))

#_(sha1-base58 "hello")


(defn var-dependencies [form]
  (let [var-name (var-name form)]
    (->> form
         (tree-seq sequential? seq)
         (keep #(when (and (symbol? %)
                           (not= var-name %))
                  (resolve %)))
         (into #{}))))

#_(var-dependencies '(def nextjournal.clerk.hashing/foo
                       (fn* ([s] (clojure.string/includes? (rewrite-clj.parser/parse-string-all s) "hi")))))

(defn analyze [form]
  (let [analyzed-form (-> form
                          ana/analyze
                          (ana.passes.ef/emit-form #{:hygenic :qualified-symbols}))
        var (some-> analyzed-form var-name resolve)
        deps (cond-> (var-dependencies analyzed-form) var (disj var))]
    (cond-> {:form form
             :analyzed-form analyzed-form
             :ns-effect? (some? (some #{#'clojure.core/require #'clojure.core/in-ns} deps))}
      var (assoc :var var)
      (seq deps) (assoc :deps deps))))

#_(analyze '(let [x 2] x))
#_(analyze '(defn foo [s] (str/includes? (p/parse-string-all s) "hi")))
#_(analyze '(defn segments [s] (let [segments (str/split s)]
                                 (str/join segments))))
#_(analyze '(v/md "It's **markdown**!"))
#_(analyze '(in-ns 'user))
#_(analyze '(do (ns foo)))
#_(analyze '(def my-inc inc))

(defn remove-leading-semicolons [s]
  (str/replace s #"^[;]+" ""))


(defn parse-file
  ([file]
   (parse-file {} file))
  ([{:as _opts :keys [markdown?]} file]
   (loop [{:as state :keys [doc nodes]} {:nodes (:children (p/parse-file-all file))
                                         :doc []}]
     (if-let [node (first nodes)]
       (recur (cond
                (#{:deref :map :meta :list :quote :reader-macro :set :token :var :vector} (n/tag node))
                (-> state
                    (update :nodes rest)
                    (update :doc (fnil conj []) {:type :code :text (n/string node)}))

                (and markdown? (n/comment? node))
                (-> state
                    (assoc :nodes (drop-while n/comment? nodes))
                    (update :doc conj {:type :markdown :text (apply str (map (comp remove-leading-semicolons n/string)
                                                                             (take-while n/comment? nodes)))}))
                :else
                (update state :nodes rest)))
       doc))))

#_(parse-file "notebooks/elements.clj")
#_(parse-file {:markdown? true} "notebooks/rule_30.clj")
#_(parse-file "notebooks/src/demo/lib.cljc")

(defn auto-resolves [ns]
  (as-> (ns-aliases ns) $
    (assoc $ :current (ns-name *ns*))
    (zipmap (keys $)
            (map ns-name (vals $)))))

#_(auto-resolves (find-ns 'rule-30))


(defn read-string [s]
  (edamame/parse-string s {:all true
                           :auto-resolve (auto-resolves (or *ns* (find-ns 'user)))
                           :readers *data-readers*
                           :read-cond :allow
                           :features #{:clj}}))
#_(read-string "(ns rule-30 (:require [nextjournal.clerk.viewer :as v]))")

(defn analyze-file
  ([file]
   (analyze-file {} {:graph (dep/graph)} file))
  ([state file]
   (analyze-file {} state file))
  ([{:as opts :keys [markdown?]} acc file]
   (let [doc (parse-file opts file)]
     (reduce (fn [acc {:keys [type text]}]
               (if (= type :code)
                 (let [form (read-string text)
                       {:as info :keys [var deps form ns-effect?]} (analyze form)]
                   (when ns-effect?
                     (eval form))
                   (cond-> (assoc-in acc [:var->info (if var var form)] (assoc info :file file :code text))
                     (seq deps)
                     (#(reduce (fn [{:as acc :keys [graph]} dep]
                                 (try (assoc acc :graph (dep/depend graph (if var var form) dep))
                                      (catch Exception e
                                        (when-not (-> e ex-data :reason #{::dep/circular-dependency})
                                          (throw e))
                                        (let [{:keys [node dependency]} (ex-data e)
                                              rec-form (concat '(do) [form (get-in acc [:var->info dependency :form])])
                                              rec-var (symbol (str var "+" dep))]
                                          (-> acc
                                              (assoc :graph (-> graph
                                                                (dep/remove-edge dependency node)
                                                                (dep/depend var rec-var)
                                                                (dep/depend dep rec-var)))
                                              (assoc-in [:var->info rec-var :form] rec-form)))))) % deps))))
                 acc))
             (cond-> acc markdown? (assoc :doc doc))
             doc))))

#_(:graph (analyze-file {:markdown? true} {:graph (dep/graph)} "notebooks/elements.clj"))
#_(analyze-file {:markdown? true} {:graph (dep/graph)} "notebooks/rule_30.clj")
#_(analyze-file {:graph (dep/graph)} "notebooks/recursive.clj")
#_(analyze-file {:graph (dep/graph)} "notebooks/hello.clj")

(defn unhashed-deps [var->info]
  (set/difference (into #{}
                        (mapcat :deps)
                        (vals var->info))
                  (-> var->info keys set)))

#_(unhashed-deps {#'elements/fix-case {:deps #{#'rewrite-clj.node/tag}}})

;; TODO: handle cljc files
(defn ns->file [ns]
  (some (fn [dir]
          ;; TODO: fix case upstream when ns can be nil because var can contain java classes like java.lang.String
          (when-let [path (and ns (str dir fs/file-separator (str/replace (str ns) "." fs/file-separator) ".clj"))]
            (when (fs/exists? path)
              path)))
        (cp/classpath-directories)))

#_(ns->file (find-ns 'nextjournal.clerk.hashing))

(def var->ns
  (comp :ns meta))

#_(var->ns #'inc)

(defn ns->jar [ns]
  (let [path (str (str/replace ns "." fs/file-separator))]
    (some #(when (or (.getJarEntry % (str path ".clj"))
                     (.getJarEntry % (str path ".cljc")))
             (.getName %))
          (cp/classpath-jarfiles))))

#_(ns->jar (var->ns #'dep/depend))

(defn symbol->jar [sym]
  (some-> (if (instance? Class sym)
            sym
            (class (cond-> sym (var? sym) deref)))
          .getProtectionDomain
          .getCodeSource
          .getLocation
          .getFile))

#_(symbol->jar io.methvin.watcher.DirectoryChangeEvent)
#_(symbol->jar #'inc)


(defn find-location [sym]
  (if (var? sym)
    (let [ns (var->ns sym)]
      (or (ns->file ns)
          (ns->jar ns)
          (symbol->jar sym)))
    (symbol->jar sym)))

#_(find-location #'inc)
#_(find-location #'dep/depend)
#_(find-location com.mxgraph.view.mxGraph)
#_(find-location String)

(def hash-jar
  (memoize (fn [f]
             {:jar f :hash (sha1-base58 (io/input-stream f))})))

#_(hash-jar (find-location #'dep/depend))

(defn build-graph
  "Analyzes the forms in the given file and builds a dependency graph of the vars.

  Recursively decends into dependency vars as well as given they can be found in the classpath.
  "
  [file]
  (let [{:as graph :keys [var->info]} (analyze-file file)]
    (reduce (fn [g [source symbols]]
              (if (or (nil? source)
                      (str/ends-with? source ".jar"))
                (update g :var->info merge (into {} (map (juxt identity (constantly (if source (hash-jar source) {})))) symbols))
                (analyze-file g source)))
            graph
            (group-by find-location (unhashed-deps var->info)))))


#_(build-graph "notebooks/hello.clj")
#_(keys (:var->info (build-graph "notebooks/elements.clj")))
#_(dep/immediate-dependencies (:graph (build-graph "notebooks/elements.clj"))  #'nextjournal.clerk.demo/fix-case)
#_(dep/transitive-dependencies (:graph (build-graph "notebooks/elements.clj"))  #'nextjournal.clerk.demo/fix-case)

#_(keys (:var->info (build-graph "src/nextjournal/clerk/hashing.clj")))
#_(dep/topo-sort (:graph (build-graph "src/nextjournal/clerk/hashing.clj")))
#_(dep/immediate-dependencies (:graph (build-graph "src/nextjournal/clerk/hashing.clj"))  #'nextjournal.clerk.hashing/long-thing)
#_(dep/transitive-dependencies (:graph (build-graph "src/nextjournal/clerk/hashing.clj"))  #'nextjournal.clerk.hashing/long-thing)

(defn hash
  ([file]
   (let [{:as analysis :keys [graph var->info]} (build-graph file)
         var->hash (reduce (fn [var->hash var]
                             (if-let [info (get var->info var)]
                               (assoc var->hash var (hash var->hash (assoc info :var var)))
                               var->hash))
                           {}
                           (dep/topo-sort graph))]
     (assoc analysis
            :code->info (into {} (keep (fn [[var hash]]
                                         (when-let [code (:code (var->info var))]
                                           [code (assoc (var->info var) :hash hash)]))) var->hash))))
  ([var->hash {:keys [hash form deps var]}]
   (let [hashed-deps (into #{} (map var->hash) deps)]
     (sha1-base58 (pr-str (conj hashed-deps (if form (cond->> form var (drop 2)) hash)))))))

#_(hash "notebooks/hello.clj")
#_(hash "notebooks/elements.clj")
#_(clojure.data/diff (hash "notebooks/how_clerk_works.clj")
                     (hash "notebooks/how_clerk_works.clj"))

(comment
  (require 'clojure.data)
  (let [file "notebooks/cache.clj"
        g1 (build-graph file)
        g2 (build-graph file)]
    [:= (= g1 g2)
     :diff (clojure.data/diff g1 g2)]))
