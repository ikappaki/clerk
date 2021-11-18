;; # 📝 Inline SVG
(ns ^:nextjournal.clerk/no-cache viewers.svg
  (:require [arrowic.core :as arrowic]
            [nextjournal.clerk :as clerk]))

(nextjournal.clerk/set-viewers!
 [{:pred (fn [m] (and (map? m) (contains? m :nextjournal/svg)))
   :fetch-fn (fn [_ {:nextjournal/keys [svg]}] svg)
   :render-fn (fn [svg] (v/html svg))}])

(defn show-graph [{:keys [graph var->hash]}]
  (-> (arrowic/create-graph)
      (arrowic/with-graph
        (let [vars->verticies (into {} (map (juxt identity arrowic/insert-vertex!)) (keys var->hash))]
          (doseq [var (keys var->hash)]
            (doseq [dep (dep/immediate-dependencies graph var)]
              (when (and (vars->verticies var)
                         (vars->verticies dep))
                (arrowic/insert-edge! (vars->verticies var) (vars->verticies dep)))))))
      arrowic/as-svg))

{:nextjournal/svg (-> "notebooks/viewers/svg.clj" nextjournal.clerk.hashing/build-graph show-graph)}

(comment
  (-> "notebooks/viewers/svg.clj" nextjournal.clerk.hashing/build-graph show-graph)
  (nextjournal.clerk/build-static-app! {:paths ["notebooks/viewers/svg.clj"]})
  (do
    (nextjournal.clerk/clear-cache!)
    (nextjournal.clerk/show! "notebooks/viewers/svg.clj")))
