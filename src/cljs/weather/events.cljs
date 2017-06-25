(ns weather.events
  (:require
    [app-re-frame.core :as re]
    )
  )

(re/reg-event-fx
  :fetch-weather
  (fn [cofx]
    {:http {:uri "/weather"
            :success [:weather-arrived]}}))

(re/reg-event-db
  :weather-arrived
  (fn [db [weather]]
    (assoc-in db [:weather] weather)))


