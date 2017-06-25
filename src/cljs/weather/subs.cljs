(ns weather.subs
  (:require
    [app-re-frame.core :as re]
    )
  )

(re/reg-sub
  :weather
  :weather)
