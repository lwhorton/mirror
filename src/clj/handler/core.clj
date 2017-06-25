(ns handler.core
  (:require
    [org.httpkit.server :as http]
    [org.httpkit.client :as client]
    [compojure.core :refer [GET defroutes]]
    [compojure.route :as route]
    [compojure.coercions :refer [as-int]]
    [ring.util.response :refer [response] :as resp]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
    [clojure.data.json :as json]
    [cognitect.transit :as t]
    )
  (:import (java.io ByteArrayOutputStream))
  )

(def api-key-darksky (:key (json/read-str (slurp "~/mirror/keys/darksky.json") :key-fn keyword)))
(def api-key-google-maps (:key (json/read-str (slurp "~/mirror/keys/google-maps.json") :key-fn keyword)))

(def darksky "https://api.darksky.net")
(def lat "*{weather-latitude}")
(def lon "*{weather-longitude}")

(def google-maps "https://maps.googleapis.com/maps/api/distancematrix/json")
(def work "{work-address}")
(def public-transit "{nearest-public-transit-address}")
(def home "{home-address}")

(defn write [x]
  (let [out (ByteArrayOutputStream.)
        w (t/writer out :json)
        _ (t/write w x)
        v (.toString out)]
    (.reset out)
    v))

(defn res->json [res]
  {:status 200
   :headers {"content-type" "application/json"}
   :body (:body res)})

(defn res->transit [res]
  {:status 200
   :headers {"content-type" "application/transit+json"}
   :body (write (json/read-str
                  (:body res)
                  :key-fn keyword))})

(defn get-weather [req]
  (http/with-channel req chan
    (let [uri (str darksky
                   "/forecast/"
                   api-key-darksky
                   "/" lat "," lon)
          res (client/get uri {:query-params {:exclude "flags,minutely"}})]
      (http/send! chan (res->transit @res)))))

(defn- minutes-from-now-unix
  ([] (minutes-from-now-unix 0))
  ([m] (+ (quot (System/currentTimeMillis) 1000) (* m 60))))

(defn get-commute [req mins-to-departure]
  (http/with-channel req chan
    (let [res (client/get google-maps {:query-params {:origins home
                                                      :destinations (clojure.string/join "|" [work public-transit])
                                                      :key api-key-google-maps
                                                      :departure_time (minutes-from-now-unix mins-to-departure)}})]
      (http/send! chan (res->transit @res)))))

(defroutes routes
  (GET "/" [] (resp/content-type (resp/resource-response "index.html") "text/html"))
  (GET "/weather" [] get-weather)
  (GET "/commute" [mins-to-departure :as r] (get-commute r (as-int (or mins-to-departure "0"))))
  (route/files "/" {:root ""})
  (route/resources "/" {:root ""})
  (route/not-found (response {:message "Page not found"}))
  )

(def app
  (-> (wrap-defaults routes api-defaults)))
