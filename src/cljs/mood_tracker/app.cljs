(ns mood-tracker.app
  (:require [reagent.core     :as reagent :refer [atom]]
            [cljs-time.core   :as t]
            [cljs-time.format :as f]
            [clojure.string   :as s]))

(defn default-day [date]
  { :date date :severity nil :notes nil })

; this is the cache
(def cache (atom {}))

; this is the current state of the overall view
(def day (atom (default-day (t/today-at 00 00))))

; here are some functions for adjusting the current global state
(defn load-day     [date]     (get @cache (str date) (default-day date)))
(defn get-day      [date]     (reset! day (load-day date)))
(defn save         []         (swap! cache assoc (str (@day :date)) @day))
(defn set-severity [severity] (swap! day assoc :severity severity))

(defn mood-card [severity number]
  [:div.two.col.l2
   [:div { :class (->> ["mood-rating" severity (when (= (@day :severity) severity) "active")]
                       (remove nil?)
                       (s/join " "))
           :on-click #(set-severity severity)}
    [:div.value    number]
    [:div.severity severity]]])

(defn previous-day [] (get-day (t/minus (@day :date) (t/days 1))))
(defn next-day     [] (get-day (t/plus  (@day :date) (t/days 1))))

(defn date-picker []
   [:div.row
    [:div.col.l10.offset-l1
     [:h4.title
      [:i.mdi-action-today]
      [:br]
      [:a.day-before { :href "#" :on-click previous-day } "◀"]
      (f/unparse (f/formatter "dd MMMM yyyy") (@day :date))
      [:a.day-after  { :href "#" :on-click next-day }     "▶"]]]])

(defn day-view []
  [:div.container
   [date-picker]
   [:div.row
    [:div.col.l1 "\xA0"]
    [mood-card "awful" "1"]
    [mood-card "bad"   "2"]
    [mood-card "ok"    "3"]
    [mood-card "good"  "4"]
    [mood-card "great" "5"]]
   [:div.row
    [:form.col.l10.offset-l1
      [:textarea.notes { :placeholder  "Record any thoughts about this day here..."
                         :value        (@day :notes)
                         :on-change   #(swap! day assoc :notes (-> % .-target .-value)) } ]]]
   [:div.row
    [:div.col.l10.offset-l1
     [:a.record.blue-grey.darken-1
      { :href "#" :on-click save }
      "Record"]]]])

(defn init []
  (reagent/render-component
    [day-view]
    (.getElementById js/document "container")))
