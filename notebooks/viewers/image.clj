;; # 🏞 Customizing Fetch
;; Showing how to use a custom `fetch-fn` with a `content-type` to let Clerk serve arbitrary things, in this case a PNG image.
(ns ^:nextjournal.clerk/no-cache fetch-image
  (:require [clojure.java.io :as io]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v])
  (:import [java.nio.file Files]))

;; We set a custom viewer for `bytes?` that includes a `:fetch-fn`, returning a wrapped value with a `:nextjournal/content-type` key set.
(clerk/set-viewers! [{:pred bytes?
                      :fetch-fn (fn [_ bytes] {:nextjournal/content-type "image/png"
                                               :nextjournal/value bytes})
                      :render-fn (fn [blob] (v/html [:img {:src (v/url-for blob)}]))}])


(Files/readAllBytes (.toPath (io/file "/Users/mk/Desktop/clojure-logo.png")))

#_(nextjournal.clerk/show! "notebooks/fetch_image.clj")
