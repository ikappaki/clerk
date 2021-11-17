;; # 🏞 Customizing Fetch
;; Showing how to use a custom `fetch-fn` with a `content-type` to let Clerk serve arbitrary things, in this case a PNG image.
(ns ^:nextjournal.clerk/no-cache image
  (:require [clojure.java.io :as io]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v])
  (:import (java.net.http HttpRequest HttpClient HttpResponse$BodyHandlers)
           (java.net URI)))

;; We set a custom viewer for `bytes?` that includes a `:fetch-fn`, returning a wrapped value with a `:nextjournal/content-type` key set.
(clerk/set-viewers! [{:pred bytes?
                      :fetch-fn (fn [_ bytes] {:nextjournal/content-type "image/png"
                                               :nextjournal/value bytes})
                      :render-fn (fn [blob] (v/html [:img {:src (v/url-for blob)}]))}])

(let [request (.. (HttpRequest/newBuilder)
                  (uri (URI. "https://upload.wikimedia.org/wikipedia/commons/5/57/James_Clerk_Maxwell.png"))
                  build)]

  (.. (HttpClient/newHttpClient)
      (send request (HttpResponse$BodyHandlers/ofByteArray))
      body))

#_(nextjournal.clerk/serve! {:watch-paths ["notebooks"]})
#_(nextjournal.clerk/show! "notebooks/viewers/image.clj")

