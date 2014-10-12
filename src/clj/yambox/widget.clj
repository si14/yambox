(ns yambox.widget
  (:use
   hiccup.core
   hiccup.page)
  (:require
   [garden.core :refer [css]]
   [plumbing.core :as p]))

(defn trunc [str n]
  (subs str 0 (min (count str) n)))

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
       [:input.sum {:height "38px"
                    :width "100px"
                    :margin-left "20px"
                    :border "1px solid #ddd"
                    :border-radius "3px"
                    :padding "0px 5px"
                    :font-size "26px"
                    :display "inline-block"}]
       [:div.vote {:margin "10px 20px 10px 10px"
                   :margin-right "0"}
        [:button.vote-btn {:background "#ff8052"
                           :display "inline-block"
                           :padding "10px 20px"
                           :border-radius "3px"
                           :outline "none"
                           :border "0px"
                           :font-size "14px"
                           :color "white"
                           :font-weight "bold"}]]]
      [:div.content {:width "100%"}
       [:a {:color "#ff8052"}]
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
     [:div.info {:display "none"
                 :position "absolute"
                 :padding "5px 20px"
                 :color "#aaa"
                 :font-size "10px"
                 :right "0"
                 :bottom "0"}
      [:a {:color "#aaa"}]]]))

(p/defnk render [slug current-money target-money start-money wallet-id name]
  (let [target-adjusted (- target-money start-money)
        current-adjusted (- current-money start-money)
        width-percent (* 100 (/ (double current-adjusted)
                                (double target-adjusted)))
        comment (trunc (str "YamBox: " name) 60)]
    (html5
     [:head {:lang "ru"}
      [:meta {:charset "UTF-8"}]
      [:style (get-css)]]
     [:body
      [:form {:method "POST"
              :action "https://money.yandex.ru/quickpay/confirm.xml"}
       [:div.banner
        [:div.row
         [:div.cell.content
          [:h1 [:a {:href (str "https://yambox.org/campaigns/" slug) :target "_blank"} name]]
          [:div.ruller
           [:span.max target-money]
           [:div.active {:style (str "width: " width-percent "%;")}
            [:span.cur current-adjusted]]]]
         [:div.cell
          [:input.sum {:type "text" :name "sum" :value 100}]]
         [:div.cell
          [:div.vote
           [:input {:type "hidden" :name "receiver" :value wallet-id}]
           [:input {:type "hidden" :name "formcomment" :value comment}]
           [:input {:type "hidden" :name "targets" :value comment}]
           [:input {:type "hidden" :name "short-dest" :value comment}]
           [:input {:type "hidden" :name "quickpay-form" :value "small"}]
           [:input {:type "hidden" :name "paymentType" :value "PC"}]
           [:button.vote-btn {:type "submit"} "Оплатить"]]]]
        [:div.info "Powered by " [:a {:href "https://yambox.org" :target "_blank"} "YamBox"]]]]])))

(defn render-empty []
  (html5
    [:head {:lang "ru"}
     [:meta {:charset "UTF-8"}]
     [:style (get-css)]]
    [:body
     [:div.banner {:style "width: 100%"}
      [:div.row {:style "text-align: center;"}
       [:div.cell
        "Виджет не найден."]]]]))
