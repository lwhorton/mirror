(ns main.core
  (:require
    [cognitect.transit :as t]
    [devtools.core :as devtools]
    [reagent.core :as r]
    [app-re-frame.core :as re]
    [goog.net.XhrIo :as xhr]
    [weather.views :refer [weather]]
    [commute.views :refer [comm]]
    )
  (:require-macros
    [aft.logging.core :refer [log]])
  )

(devtools/install! [:formatters :hints :async])

(def reader (t/reader :json))

(defn root []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :flex 1
                 :margin "5em"}}
   [weather]
   [comm]
   ])

(defn make-uri
  ([uri]
   (make-uri uri nil))
  ([uri params]
   (let [p (reduce #(str %1 (name (first %2)) "=" (second %2) "&") "?" params)
         query (subs p 0 (- (count p) 1))]
     (str uri query))))

(re/reg-fx
  :http
  (fn [effect]
    (xhr/send (make-uri (get effect :uri) (get effect :params))
              ;; callback
              (fn [e]
                (let [res (t/read reader (-> e .-target .getResponseText))]
                  (re/dispatch [(first (get effect :success)) res])))

              ;; method
              "GET"
              ;; content
              ;; headers
              ;; timeout
              )))

(defn start-reload []
  (js/setTimeout #(.reload (.-location js/window)) (* 1000 60 60 24)))

(defn main []
  (start-reload)
  (re/dispatch-sync [:initialized])
  (r/render [root] (.getElementById js/document "app")))
