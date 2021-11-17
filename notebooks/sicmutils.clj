;; # Clerk meets SICMUtils ⚛️

;; ## Todos:
;; - [ ] call simplify by default (?)
;; - [ ] decide if aliases are needed in set-viewers after bootstrap-repl
;; - [ ] fix the some-fn to make pred simpler
;; - [ ] some equations produce katex errors
;; - [ ] fix stackoverflow on ∞-taylor expansion series (?)

(ns sicmutils
  (:require [nextjournal.clerk :as clerk]
            [sicmutils.env :as env]
            [sicmutils.expression :as e]
            [sicmutils.structure :as s]
            [sicmutils.function :as f]
            [sicmutils.expression.render :as e.render]))

(clerk/set-viewers!
 [{:pred (fn [x] (or (e/literal? x)
                     (s/structure? x)
                     #_(f/function? x) ;; won't render latex correctly
                     (symbol? x)))
   :transform-fn e.render/->TeX
   :render-fn (fn [latex] (v/katex-viewer latex))}])

(env/bootstrap-repl!)

'Pi

((+ (square sin) (square cos)) 't)

(simplify ((+ (square sin) (square cos)) 't))

(first (((exp D) sin) 'x))


;; 🔴 this stackoverflows and it's not elided
(take 10
      (((exp D) sin) 'x))


;; ### Automatic Diff
(simplify ((D cube) 'x))

;; ### Lagrangian Equations
(defn L-central-polar [m U]
  (fn [[_ [r] [rdot thetadot]]]
    (- (* 1/2 m
          (+ (square rdot)
             (square (* r thetadot))))
       (U r))))

(let [potential-fn (literal-function 'U)
      L (L-central-polar 'm potential-fn)
      state (up (literal-function 'r)
                (literal-function 'theta))]
  (((Lagrange-equations L) state) 't))


;; ### Structures
(down
 'alphadot_beta
 'xdotdot
 'zetaprime_alphadot
 'alphaprimeprime_mubar
 'vbar
 'Pivec
 'alphatilde)

(* (down 'x 'y 'z) (up 'a 'b 'c))

(cross-product ['x 'y 'z] ['a 'b 'c])

(def r (literal-function 'r))
(def theta (literal-function 'theta))

(defn e [t] ((up cos sin) t))

(defn c [t] ((up (- sin) cos) t))


;; tangential unit
((e theta) 't)

;; central unit
((c theta) 't)

(simplify ((D (e theta)) 't))

(simplify ((* (D theta) (c theta)) 't))

(= (simplify ((D (e theta)) 't))
   (simplify (* (D theta) (c theta))))

(defn tm [t] ((* r (e theta)) t))

(def tv "tangential velocity" (D tm))

(tm 't)

(simplify (tv 't))

((simplify tv) 't)

(def tv' (simplify (+ (* (D r) (e theta)) (* r (D theta) (c theta)))))


(tv' 't)

(= tv tv')
(= (tv 't) (tv' 't))

(simplify ((D tv) 't))

(comment

  (clerk/tex (->TeX ((+ (square sin) (square cos)) 't)))

  (clerk/serve! {:watch-paths ["notebooks"]})

  (type (+ (square sin) (square cos)))


  (type ((+ (square sin) (square cos)) 't))


  (s/structure? (up 'a 'b))

  (e/literal? (up 'a 'b))
  (s/structure? 1)
  (e.render/->TeX (up
                   'alphadot_beta
                   'xdotdot
                   'zetaprime_alphadot
                   'alphaprimeprime_mubar
                   'vbar
                   'Pivec
                   'alphatilde))

  (do (clerk/clear-cache!)
      (clerk/show! "notebooks/sicmutils.clj"))

  )
