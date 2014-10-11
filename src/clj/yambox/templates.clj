(ns yambox.templates
  (:use hiccup.core
        hiccup.page))

(defn head
  [title]
  [:head
   [:title title]
   (include-css "css/style.css")])

(defn page-create
  []
  (html5
    (head "title")
    [:body "test"]))