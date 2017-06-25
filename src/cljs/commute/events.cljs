(ns commute.events
  (:require
    [app-re-frame.core :as re]
    )
  )

(re/reg-event-db
  :commute-arrived
  (fn [db [travel]]
    (assoc-in db [:commute :travel] travel)))

(re/reg-event-fx
  :fetch-commute
  (fn [cofx]
    {:http {:uri "/commute"
            :success [:commute-arrived]
            :params {:mins-to-departure 30}}
     :db (assoc-in (:db cofx) [:commute :mins-to-departure] 30)}))


