(ns app-re-frame.core
  (:require
    [re-frame.core :as reframe]
    )
  (:require-macros
    [aft.logging.core :refer [log error]]
    )
  )

(def default-interceptors [
                           ;(when ^:boolean goog.DEBUG debug-lite)
                           ;(when ^:boolean goog.DEBUG (reframe/after valid-schema?))
                           ;(when ^:boolean goog.DEBUG (reframe/after valid-spec?))
                           reframe/trim-v
                           ])

(defn reg-sub
  [& args]
  (apply reframe/reg-sub args))

(defn reg-fx
  [& args]
  (apply reframe/reg-fx args))

(defn subscribe
  [& args]
  (apply reframe/subscribe args))

(defn dispatch
  [& args]
  (apply reframe/dispatch args))

(defn dispatch-sync
  [& args]
  (apply reframe/dispatch-sync args))

(defn reg-event-db
  "Register a db handler that automatically uses app-wide default interceptors.
  Optionally provide additional interceptors (as a vec) to include after the
  default."
  ([id handler]
   (reframe/reg-event-db id default-interceptors handler))
  ([id more handler]
   (if-not (vector? more)
     (error "re-frame: registering a handler with additional
            interception requires that interceptor to be inside a vector.")
     ;; re-frame will flatten and remove nil, so nested vecs is fine
     (reframe/reg-event-db id [default-interceptors more] handler))))

(defn reg-event-fx
  "Register an fx handler that automatically uses app-wide default interceptors.
  Optionally provide additional interceptors (as a vec) to include after the
  default."
  ([id handler]
   (reframe/reg-event-fx id default-interceptors handler))
  ([id more handler]
   (if-not (vector? more)
     (error "re-frame: registering a handler with additional
            middleware requires that middleware to be inside a vector.")
     ;; re-frame will flatten and remove nil, so nested vecs is fine
     (reframe/reg-event-fx id [default-interceptors more] handler))))
