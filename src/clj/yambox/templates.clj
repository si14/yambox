(ns yambox.templates
  (:use hiccup.core
        hiccup.page)
  (:require [plumbing.core :as p]))

(defn- wrap-head
  [title]
  [:head {:lang "ru"}
   [:meta {:charset "UTF-8"}]
   [:title title]
   (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css")
   (include-css "http://fonts.googleapis.com/css?family=PT+Sans+Narrow&subset=latin,cyrillic")
   (include-css "http://fonts.googleapis.com/css?family=PT+Sans:400,700&subset=cyrillic,latin")
   (include-css "css/style.css")])

(defn- wrap-top
  [& links]
  [:div.top
   [:img {:src "img/logo_small.png"}]
   [:div.links links]
   [:div.clear]])

(defn- wrap-yandex-social
  []
  [:div.social
   [:script
    {:charset "utf-8",
     :src     "//yastatic.net/share/share.js",
     :type    "text/javascript"}]
   [:div.yashare-auto-init
    {:data-yasharetheme "counter",
     :data-yasharequickservices
                        "vkontakte,facebook,twitter,odnoklassniki,moimir,gplus",
     :data-yasharel10n  "ru"}]])

(p/defnk make-html
  [title body :as p]
  (html5
    (wrap-head title)
    body))

(defn page-index
  []
  (make-html
    {:title "YamBox"
     :body  [:div
             (wrap-top [:a {:href "/login"} "Войти в аккаунт"])
             [:div.landing
              [:div.info
               [:h1 "Собирай деньги проще и эффективнее!"]
               "YamBox — удивительный инструмент для коллективного сбора денег. "
               "Собирай так, чтобы все были довольны."
               [:p
                [:a.btn.btn-default
                 {:href "/management/create-campaign"}
                 "Начать кампанию сейчас!"]]]
              [:div.copyright
               "Image by "
               [:a {:href   "https://www.flickr.com/photos/hktang/5369651068/",
                    :target "_blank"}
                "Xiaojun Deng"]
               ". Licenced under Creative Commons 2.0."]]
             (wrap-yandex-social)]}))

(defn page-management
  [req]
  (make-html
    {:title "Управление кампанией — YamBox"
     :body [:div
            (wrap-top [:a {:href "/logout"} "Выйти"])

            ]}))

(defn page-management-create
  [req]
  (make-html
    {:title "Test"
     :body "Wow"}))