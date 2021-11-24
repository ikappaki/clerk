;; # Controlling Visibility
;; This notebook defines `^:nextjournal.clerk/hide-code` on the namespace level, so all cells are hidden unless specified otherwise.

(ns ^:nextjournal.clerk/fold-code
    visibility
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]))

;; Specific forms can opt in to having things displayed via metadata.
;; Show the result via `^{:nextjournal.clerk/fold-code false}`.
^{:nextjournal.clerk/fold-code false} (+ 42)

;; Completely hide code without the ability to unhide it is possible using `^:nextjournal.clerk/hide-code`.
^::clerk/hide-code
(do "this is top secret code and will never be shown"
    :result/generated-by-hidden-code)


;; Show the code via `^{:nextjournal.clerk/hide-code false}`.
^{::clerk/hide-code false} (+ 39 3)


^{::clerk/fold-code false} (do :bam :code/folded)

;; This has the

^{::clerk/hide-result false}
(inc 51)

#_(nextjournal.clerk/show! "notebooks/visibility.clj")
