# 📝 Inline Results

```clojure
^:nextjournal.clerk/no-cache
(ns inline-results
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v]))
```

```clojure
(defn red [text] (clerk/html [:strong [:em.ml-1.mr-1.underline.red text]]))
```

    Markdown monospace marks are actually evaluated `(red "this is some spacious red text")` inline.

Markdown monospace marks are actually evaluated `(red "this is some spacious red text")` inline.


```clojure
(deftype Slider [min max])
```

```clojure
(defonce store (atom 0))
```

    This is inline `@store` and a `(->Slider 1 20)` for ...

This is inline `@store` and a `(->Slider 1 20)` for reactively changing values in-text.


```clojure
(defn reset-store! [n]
  (reset! store n)
  (clerk/show! "notebooks/inline_results.md"))
```

```clojure
(nextjournal.clerk/set-viewers!
 [{:pred #(instance? Slider %)
   :fetch-fn (fn [_ slider] slider)
   :transform-fn (fn [x] {:min (.-min x) :max (.-max x)})
   :render-fn (fn [{:as o :keys [min max]}] 
                (v/html 
                 [:input {:type "range"
                          :min (str min)
                          :max (str max)
                          :on-change (fn [e]
                                      (v/clerk-eval 
                                       (list 'reset-store! 
                                             (.. e -target -value))))}]))}])
```

