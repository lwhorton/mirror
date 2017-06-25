(ns main.events
  (:require
    [app-re-frame.core :as re]
    )
  )

(re/reg-event-fx
  :initialized
  (fn [cofx]
    {:dispatch-n [
                  [:fetch-weather]
                  [:fetch-commute]
                  ]}))


