(ns weather.views
  (:require
    [libs.skycons]
    [cljsjs.moment]
    [app-re-frame.core :as re]
    [reagent.core :as r]
    )
  )

(defn start-refresh []
  (js/setTimeout #(re/dispatch [:fetch-weather]) (* 1000 60)))

(def moment js/moment)

(def *time-formatter* "HH:mm")
(def *date-formatter* "dddd, MMMM Do")

(def skycons (atom nil))

(defn init-skycons! []
  (reset! skycons (js/Skycons. (clj->js {:color "white"})))
  (.play @skycons))

(defn round [x]
  (.round js/Math x))

(def css {:h1 {:font-size "5em"}
          :h2 {:font-size "3em"}
          :h3 {:font-size "2em"}})

(defn angle->bearing [x]
  (let [angle (round x)
        quarters (map set (partition (/ 360 4) (range 360)))
        bearings {0 "N"
                  1 "E"
                  2 "S"
                  3 "W"}
        belongs (map-indexed #(if (contains? %2 angle) %1 nil) quarters)]
    (get bearings (first (filter identity belongs)))))

(defn windspeed [speed bearing]
  [:div
   (if (= 0 speed)
     [:span "No wind"]
     [:div
      [:span (str (round speed) " mph")]
      [:span {:style {:padding-left ".25em"}}
       (angle->bearing bearing)]])])

(defn skycon [icon & [{:keys [width height]}]]
  (when icon
    (let [id (str (random-uuid))]
      (r/create-class
        {:component-did-mount
         (fn [this] (.add @skycons id icon))
         :reagent-render
         (fn []
           [:canvas {:id id
                     :width (or width "64")
                     :height (or height "64")}])}))))

(defn low-high [low high]
  [:div {:style {:display "flex"}}
   [:div {:style {:display "flex"
                  :align-items "center"}}
    [:img {:src "down-chevron.svg"
           :style {:width "1em"
                   :height "1em"}}]
    [:span {:style {:padding "0 0 0 .25em"}} (str (round low) "°")]]
   [:div {:style {:display "flex"
                  :align-items "center"
                  :padding "0 0 0 .5em"}}
    [:img {:src "down-chevron.svg"
           :style {:transform "rotate(180deg)"
                   :width "1em"
                   :height "1em"}}]
    [:span {:style {:padding "0 0 0 .25em"}} (str (round high) "°")]]])

(defn clock []
  (let [interval (atom nil)
        tme (r/atom (moment))]
    (r/create-class
      {:component-did-mount
       (fn []
         (reset! interval (js/setInterval #(reset! tme (moment)) (* 1 1000))))
       :component-will-unmount
       (fn []
         (js/clearInterval @interval))
       :reagent-render
       (fn []
         [:div {:style {:display "flex"
                        :flex-direction "column"}}
          [:h2 {:style (:h2 css)}
           (.format @tme *date-formatter*)]
          [:h1 {:style (:h1 css)}
           (.format @tme *time-formatter*)]])})))

(defn sundown [t]
  [:div {:style {:display "flex"
                 :align-items "center"}}
   [:h3 {:style (:h3 css)}
    (.format (.unix moment t) *time-formatter*)]
   [:img {:src "sundown.svg"
          :style {:height "2em"
                  :width "2em"}}]])


(defn weather []
  (let [weather (re/subscribe [:weather])
        ;; @TODO poperly despose of these
        _ (init-skycons!)
        _ (start-refresh)]
    (fn []
      [:div {:style {:display "flex"
                     :flex 1}}
       [:div {:style {:display "flex"
                      :flex 1
                      :flex-direction "column"}}
        [clock]]

       [:div {:style {:align-items "flex-end"
                      :display "flex"
                      :flex 1
                      :flex-direction "column"
                      :max-width "15em"
                      }}
        [:div {:style {:align-items "center"
                       :display "flex"}}
         [skycon (get-in @weather [:currently :icon])]
         [:h1 {:style (merge (:h1 css)
                             {:padding "0 0 0 1rem"}
                             )}
          (str (round (get-in @weather [:currently :temperature])) "°")]]

        [:div  {:style {:display "flex"
                        :flex-direction "column"}}

         ;; sundown
         [:div {:style {:display "flex"
                        :justify-content "flex-end"
                        :padding "1em 0"}}
          [sundown (get-in @weather [:daily :data 0 :sunsetTime])]]

         ;; low / high
         [:div {:style {:display "flex"
                        :align-items "flex-end"
                        :flex-direction "column"
                        :padding-bottom "1em"}}
          [low-high
           (get-in @weather [:daily :data 0 :temperatureMin])
           (get-in @weather [:daily :data 0 :temperatureMax])]
          [:div {:style {:padding-top ".25em"}}
           [windspeed
            (get-in @weather [:daily :data 0 :windSpeed])
            (get-in weather [:daily :data 0 :windBearing])
            ]]]

         ;; summary text
         [:p {:style {:text-align "right"}} (get-in @weather [:hourly :summary])]

         ;; tomorrow
         [:div {:style {:align-items "flex-end"
                        :display "flex"
                        :flex-direction "column"
                        :padding "5em 0 0 0"}}

          [:h1 {:style {:padding "0 0 1em 0"}}
           "Tomorrow"]

          [:div {:style {:display "flex"
                         :align-items "flex-end"
                         :flex-direction "column"
                         :padding-bottom "1em"}}
           [low-high
            (get-in @weather [:daily :data 1 :temperatureMin])
            (get-in @weather [:daily :data 1 :temperatureMax])]
           [:div {:style {:padding-top ".25em"}}
            [windspeed
             (get-in @weather [:daily :data 1 :windSpeed])
             (get-in weather [:daily :data 1 :windBearing])
             ]]]

          ;; tomorrow summary
          [:p {:style {:text-align "right"}} (get-in @weather [:daily :data 1 :summary])]]
         ]]])))
