(ns commute.views
  (:require
    [app-re-frame.core :as re]
    )
  )

(defn start-refresh []
  (js/setTimeout #(re/dispatch [:fetch-commute]) (* 1000 60)))

(defn round [x]
  (.round js/Math x))

(defn get-duration [com i]
  (get-in com [:rows 0 :elements i :duration :value]))

(defn get-duration-traffic [com i]
  (get-in com [:rows 0 :elements i :duration_in_traffic :value]))

(defn get-duration-traffic-hu [com i]
  (get-in com [:rows 0 :elements i :duration_in_traffic :text]))

(defn calc-traffic-diff [com i]
  (let [dura (get-duration-traffic com i)
        diff (- dura (get-duration com i))]
    (round (/ diff 60))))

(defn- traffic-diff [com i]
  (let [diff (calc-traffic-diff com i)]
    [:div {:style {:padding "0 0 0 .5em"}}
     (if (< 0 diff)
       [:span (str "(" diff " mins of traffic)")]
       [:span "(no traffic)"])]))

(defn- to-work [com]
  [:div {:style {:display "flex"
                 :flex 1
                 :padding ".25em 0 0 0"
                 }}
   [:span (str "Work - about " (get-duration-traffic-hu com 0))]
   [traffic-diff com 0]])

(defn- to-lightrail [com]
  [:div {:style {:display "flex"
                 :flex 1
                 :padding ".25em 0 0 0"
                 }}
   [:span (str "Lightrail - about " (get-duration-traffic-hu com 1))]
   [traffic-diff com 1]])

(defn comm []
  (let [com (re/subscribe [:commute])
        _ (start-refresh)]
    (fn []
      [:div {:style {:display "flex"
                     :flex-direction "column"}}
       [:span (str "Leaving in " (:mins-to-departure @com) " minutes")]
       [:div  {:style {:display "flex"
                       :flex-direction "column"
                       }}
        [to-work (:travel @com)]
        [to-lightrail (:travel @com)]]])))
