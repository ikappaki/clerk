;; <h1 class="sans-serif text-7xl text-center">
;;   <span class="relative" style="bottom: -10px;">Clerk</span>
;;   🧑‍🔬
;; </h1>
;; <h2 class="sans-serif text-2xl text-center mt-6">Local-First Notebooks for Clojure</h2>
;; <div class="mt-6 flex items-center justify-center sans-serif text-xl">
;;   <a href="https://github.com/nextjournal/clerk" class="mr-6">
;;     🐙 GitHub
;;   </a>
;;   <a href="https://clojars.org/io.github.nextjournal/clerk" class="mr-6">
;;     📦 Clojars
;;   </a>
;;   <a href="https://github.com/nextjournal/clerk-demo" class="mr-6">
;;     🧪 Demo Repo
;;   </a>
;;   <a href="https://github.com/nextjournal/clerk/blob/fdf5206fb60fe9230da5b92dbc4b26b504e8b759/CHANGELOG.md#02209-2021-11-03">
;;     🪵 Changelog
;;   </a>
;; </div>

;; ---

;; <div class="text-center">
;;   <p>Clerk takes a Clojure namespace and turns it into a notebook:</p>
;;   <img src="https://nextjournal.com/data/QmdHmfSEZWqRwsFSDju4nLqgfVukYb3UCVp6AFGk1JcrCH?content-type=image/png&node-id=2bf0921f-0943-43c7-baaa-5c1a59937f40&filename=CleanShot%202021-07-01%20at%2012.48.22@2x.png&node-kind=file">
;; </div>

;; ## 🖼 Examples
;; <div class="grid">
;;   <div></div>
;; </div>

;; ## ⚡️ Static Builds

;; ## 🧩 Extensible Viewers API

;; ## ⚖️ Rationale
;; Computational notebooks allow arguing from evidence by mixing prose with executable code. For a good overview of problems users encounter in traditional notebooks like Jupyter, see [I don't like notebooks](https://www.youtube.com/watch?v=7jiPeIFXb6U) and [What’s Wrong with Computational Notebooks? Pain Points, Needs, and Design Opportunities](https://web.eecs.utk.edu/\~azh/pubs/Chattopadhyay2020CHI_NotebookPainpoints.pdf).
;;
;; Specifically Clerk wants to address the following problems:
;;
;; * Less helpful than my editor
;; * Notebook code being hard to reuse
;; * Reproduction problems coming from out-of-order execution
;; * Problems with archival and putting notebooks in source control
;;
;; Clerk is a notebook library for Clojure that aims to address these problems by doing less, namely:
;;
;; * no editing environment, folks can keep using the editors they know and love
;; * no new format: Clerk notebooks are regular Clojure namespaces (interspersed with markdown comments). This also means Clerk notebooks are meant to be stored in source control.
;; * no out-of-order execution: Clerk notebooks always evaluate from top to bottom. Clerk builds a dependency graph of Clojure vars and only recomputes the needed changes to keep the feedback loop fast.
;; * no external process: Clerk runs inside your Clojure process, giving Clerk access to all code on the classpath.
;;
;; **Clerk is ALPHA** so expect breaking changes.
;;
;; <div class="mt-10"><hr></div>
;;
;; ## 📦 Using Clerk
;;
;; To use Clerk in your project, add the following dependency to your `deps.edn`:
;; ```clojure
;; {:deps {io.github.nextjournal/clerk {:mvn/version "0.2.214"}}}
;; ```
;;
;; Require and start Clerk as part of your system start, e.g. in `user.clj`:
;;
;; ```clojure
;; (require '[nextjournal.clerk :as clerk])
;;
;; ;; start Clerk's buit-in webserver on the default port 7777, opening the browser when done
;; ;;(clerk/serve! {:browse? true})
;;;
;; ;; either call `clerk/show!` explicitly
;; (clerk/show! "notebooks/rule_30.clj")
;;
;; ;; or let Clerk watch the given `:paths` for changes
;; (clerk/serve! {:watch-paths ["notebooks" "src"]})
;;
;; ;; start with watcher and show filter function to enable notebook pinning
;; (clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.string/starts-with? % "notebooks")})
;; ```
;;
;; You can then access Clerk at <http://localhost:7777>.
;;
;; See the [/notebooks folder](https://github.com/nextjournal/clerk/tree/main/notebooks) in the Clerk repository for a number of sample notebooks.
;;
;; ## 🛠 Developing Clerk
;; Make sure you have [Babashka installed](https://github.com/babashka/babashka#installation), and run:

;; ```bash
;; bb dev
;; ```

;; This will start everything needed to develop Clerk. You can connect your favorite editor to it using nREPL.

;; ## 🐞 Known Issues

;; See [notebooks/onwards.clj](https://github.com/nextjournal/clerk/blob/main/notebooks/onwards.clj).
