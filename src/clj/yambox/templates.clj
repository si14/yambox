(ns yambox.templates
  (:use hiccup.core
        hiccup.page)
  (:require [plumbing.core :as p]
            [clj-time.local :as l]
            [clj-time.format :as f]))

(defn- wrap-head
  [title]
  [:head {:lang "ru"}
   [:meta {:charset "UTF-8"}]
   [:title title]
   [:link {:rel "icon" :type "image/png" :href "/img/icon.png"}]
   (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css")
   (include-css "http://fonts.googleapis.com/css?family=PT+Sans+Narrow&subset=latin,cyrillic")
   (include-css "http://fonts.googleapis.com/css?family=PT+Sans:400,700&subset=cyrillic,latin")
   (include-css "/css/style.css")])

(defn- wrap-top
  [& links]
  [:div.top.container
   [:img {:src "/img/logo_small.png"}]
   [:div.links links]
   [:div.clear]])

(defn- wrap-footer
  [& links]
  [:div.footer
   "&copy 2014 "
   [:b "Sugar Glider"]
   " team for "
   [:a {:href   "https://tech.yandex.ru/events/meetings/october-2014/"
        :target "_blank"}
    "Yandex.Money Hackathon"]])

(defn- wrap-yandex-social
  []
  [:div.social.container
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
               [:h1 "Если не мой кошелёк, то чей?"]
               "Собираете деньги на добрые дела? Добро должно быть открытым."
               [:p
                [:a.btn.btn-default
                 {:href "/management/create-campaign"}
                 "Я тоже так считаю"]]]
              [:div.copyright
               "Image by "
               [:a {:href   "https://www.flickr.com/photos/hktang/5369651068/",
                    :target "_blank"}
                "Xiaojun Deng"]
               ". Licenced under Creative Commons 2.0."]]
             (wrap-yandex-social)
             [:div.features
              [:div.container
               [:div.col-sm-3]
               [:div.col-sm-6
                [:h2 "YamBox это:"]
                [:ol
                 [:li "удобный виджет, показывающий количество собранных денег;"]
                 [:li "открытая статистика, анонимизирующая жертвователей;"]
                 [:li "способ безопасно передать код протекции собирающему;"]
                 [:li "«голосование деньгами»."]]
                ]
               [:div.col-sm-3]]]
             [:div.openmind
              [:div.container
               [:div.col-sm-2]
               [:div.col-sm-8
                [:h2 "Открытость"]
                "Мы считаем, что crowdfunding должен быть прозрачным. Для этого мы реализовали
                виджет, отображающий текущее состояние счёта, и открытую статистику переводов
                в удобном для людей и компьютеров виде."]
               [:div.col-sm-2]]]
             [:div.security
              [:div.container
               [:div.col-sm-2]
               [:div.col-sm-8
                [:h2 "Безопасность"]
                [:p
                 "Не все люди добры так же, как мы и вы, поэтому мы серьёзно относимся к
                 безопасности жертвователей. Сразу после получения информации о переводах мы
                 криптографически анонимизируем их, делая невозможным сопоставление записи о
                 переводе с конкретным человеком. Вместе с тем мы сохраняем уникальность
                 записей, чтобы любой мог убедиться в честности сбора."]
                [:p
                 "Для значительных пожертвований мы предоставляем дополнительную меру
                 безопасности: вы можете совершить перевод с кодом протекции и передать его
                 организатору кампании. Так как указанный код протекции оказывается зашифрован
                 в браузере отправителя, прочитать его не сможет никто, кроме адресата — даже мы."]
                [:p [:b
                     "И последнее: никто не любит, когда его подслушивают. Именно поэтому ваше соединение
                     с YamBox "
                     [:a {:target "_blank"
                          :href "https://www.ssllabs.com/ssltest/analyze.html?d=yambox.org"}
                      "имеет рейтинг безопасности A+"]
                     "."]]]
               [:div.col-sm-2]]]
             [:div.choose
              [:div.container
               [:div.col-sm-2]
               [:div.col-sm-8
                [:h2 "Право выбора"]
                [:p
                 "Вы тоже считаете, что иметь выбор — это хорошо? Голосуйте своими деньгами:"]
                [:iframe {:src "/campaigns/yambox-donate/widget"
                          :width "100%"
                          :frameborder "0"
                          :allowtransparency "true"
                          :scrolling "no"}]
                [:p
                 [:a.btn.btn-default
                  {:href "/management/create-campaign"}
                  "Хочу себе такой виджет"]]]
               [:div.col-sm-2]]]
             (wrap-footer)]}))

(defn page-management
  [req campaign]
  (let [campaign-link (str "https://yambox.org/campaigns/" (:slug campaign))
        widget-link (str campaign-link "widget")]
    (make-html
     {:title "Управление кампанией — YamBox"
      :body [:div
             (wrap-top [:a {:href "/logout"} "Выйти"])
             [:div.separator]
             [:div.container.page
              [:div.col-sm-8.form
               [:form {:role "form" :method "POST"}
                [:div.form-group
                 [:input.header.control {:value (:name campaign)
                                         :name "name"}]]
                [:div.form-group
                 [:label "Ссылка на страницу кампании:"]
                 [:input.control {:name "slug"
                                  :value (:slug campaign)
                                  :disabled "disabled"}]]
                [:div.form-group
                 [:label "Сумма для сбора (в рублях):"]
                 [:input.control {:type "number"
                                  :name "target-money"
                                  :value (:target-money campaign)
                                  :disabled "disabled"}]]
                [:input.btn.btn-default {:type "submit" :value "Обновить"}]]]
              [:div.col-sm-4.tips
               [:p
                "Ваша кампания доступна по ссылке "
                [:a {:href campaign-link} campaign-link]]
               [:p "Код для виджета:"
                [:pre (str "&lt;iframe src=" widget-link
                           " width=\"100%\""
                           " frameborder=\"0\""
                           " allowtransparency=\"true\""
                           " scrolling=\"no\""
                           "&gt;&lt;/iframe&gt;")]]]]
             (wrap-footer)]})))

(defn page-management-create
  [req]
  (make-html
    {:title "Создание кампании — YamBox"
     :body [:div
            (wrap-top [:a {:href "/logout"} "Выйти"])
            [:div.separator]
            [:div.container.page
             [:div.col-sm-8.form
              [:form {:role "form" :method "POST"}
               [:div.form-group
                [:input.header.control {:placeholder "Введите название"
                                        :name "name"}]
                [:div.descr "Например, "
                 [:a "Сбор денег для проведения избирательной кампании"]]]
               [:div.form-group
                [:label "Ссылка на страницу кампании:"]
                [:input.control {:name "slug"}]]
               [:div.form-group
                [:label "Сумма для сбора (в рублях):"]
                [:input.control {:type "number"
                                 :name "target-money"}]]
               [:input.btn.btn-default {:type "submit" :value "Создать"}]]]
             [:div.col-sm-4.tips
              "Обратите внимание, что вы можете вести только одну кампанию одновременно.
              После создания кампании вы можете поменять название и сумму для сбора,
              однако изменение ссылки будет недоступно."]]
            (wrap-footer)]}))

(defn page-campaign
  [req op]
  (make-html
    {:title ""
     :body  [:div
             (include-js "http://code.highcharts.com/2.2/highcharts.js")
             (wrap-top [:a {:href "/logout"} "Выйти"])
             [:div.separator]
             [:div.container.page
              [:div.col-sm-12.campaign-info
               [:h1 "Название кампании"]
               [:div.ruller
                [:span.max "100 000"]
                [:div.active {:style "width: 39%;"}
                 [:span.cur "38 800"]]]]]
             [:div.container.page
              [:div.col-sm-6.operations
               (for [item op]
                 [:div.operation
                  [:div.descr
                   (:title item)
                   [:p.date (f/unparse
                              (f/formatter "dd.MM.yyyy HH:mm")
                              (l/to-local-date-time (:datetime item)))]]
                  [:div.amount (:amount item) " ₽"]
                  [:div.clear]])]
              (let [total (reduce #(+ %1 (:amount %2)) 0 op)
                    average (/ total (count op))
                    max-op (apply max-key :amount op)
                    min-op (apply min-key :amount op)
                    stats [{:name "Средняя сумма перевода" :val (Math/floor average)}
                           {:name "Максимальный перевод" :val (:amount max-op)}
                           {:name "Минимальный перевод" :val (:amount min-op)}]]
                [:div.col-sm-6.right-block
                 [:div.stats
                  (for [stat stats]
                    [:div.stat
                     [:div.descr (:name stat) ":"]
                     [:div.amount (:val stat) " ₽"]
                     [:div.clear]])]
                 [:div.graphs
                  [:h3 "Распределение платежей"]
                  [:div#histogram ""]]
                 (wrap-yandex-social)])]
             (wrap-footer)
             [:script "var columns = [['Платеж', 'Сумма'],"
              (apply str
                (for [item op]
                  (str "['" (:title item) "', " (:amount item) "],")))
              "];"]
             (include-js "https://www.google.com/jsapi")
             (include-js "/histogram.js")]}))
