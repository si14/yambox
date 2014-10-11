(ns yambox.widget
  (:use hiccup.core
        hiccup.page)
  (:require [garden.core :refer [css]]
            [plumbing.core :as p]))

(defn- get-css
  []
  (css
    [:* {:margin 0
         :padding 0}]
    [:body {:min-width "280px"
            :line-height "1.3"
            :font-family "\"Helvetica Neue\", Helvetica, Arial, sans-serif"}]
    [:div.banner {:max-width "900px"
                  :border "1px solid #ddd"
                  :height "100px"
                  :display "table"
                  :padding "0 20px"
                  :position "relative"
                  :background "linear-gradient(#eee, #fff)"}
     [:div.row {:display "table-row"}
      [:div.cell {:display "table-cell"
                  :vertical-align "middle"}
       [:div.vote {:margin "10px 20px"
                   :margin-right "0"}
        [:a.vote-btn {:background "#ff8052;"
                      :display "inline-block"
                      :padding "10px 20px"
                      :border-radius "3px"
                      :color "white"
                      :font-weight "bold"}]]]
      [:div.content {:width "100%"}
       [:h1 {:font-size "14px" }]
       [:div.ruller {:width "100%"
                     :height "18px"
                     :background "#b5acac"
                     :color "white"
                     :position "relative"
                     :border-radius "3px"
                     :font-weight "bold"
                     :margin "5px 0"}
        [:span.max :span.cur
         {:position "absolute"
          :right "0"
          :top "0"
          :font-size "12px"
          :padding "2px 4px"}]
        [:div.active {:background "#ff8052"
                      :position "absolute"
                      :height "18px"
                      :left "0"
                      :top "0"
                      :border-radius "3px"}]]]]
     [:div.info {:position "absolute"
                 :padding "5px 20px"
                 :color "#aaa"
                 :font-size "11px"
                 :right "0"
                 :bottom "0"}
      [:a {:color "#aaa"}]]]))

(p/defnk render
  [params :as req]
  (html5
    [:head {:lang "ru"}
     [:meta {:charset "UTF-8"}]
     [:style (get-css)]]
    [:body
     [:div.banner
      [:div.row
       [:div.cell.content
        [:h1 (:slug params)]
        [:div.ruller
         [:span.max "100 000"]
         [:div.active {:style "width: 39%;"}
          [:span.cur "38 800"]]]]
       [:div.cell
        [:div.vote
         [:a.vote-btn {:href "http://ya.ru/"} "Оплатить"]]]]
      [:div.info "Powered by " [:a {:href "https://yambox.org" :target "_blank"} "YamBox"]]]]))
