(ns yambox.core
  (:require
    [cemerick.friend :as friend]
    [cemerick.friend
     [workflows :as workflows]
     [credentials :as creds]]
    [cheshire.core :as json]
    [clj-http.client :as http]
    [compojure.core :as c]
    [compojure.route :as route]
    [hiccup.core :as h]
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.util.response :as resp]
    [ring.middleware.anti-forgery :as raf]
    [ring.middleware.defaults :as rmd]
    [yambox.templates :as tpl])
  (:gen-class))

(defn static-html
  [file-name]
  (-> file-name
    (resp/resource-response {:root "public"})
    (assoc :headers {"Content-Type" "text/html"})))

(c/defroutes routes
  (c/GET "/" [] (static-html "index.html"))
  (c/GET "/create" [] (tpl/page-create))
  (route/resources "/")
  (route/not-found "Page not found"))

(def handler
  (->
    (c/routes routes)
    (rmd/wrap-defaults (-> rmd/site-defaults
                         (assoc-in [:security :anti-forgery] false)))))

(defn -main
  [& args]
  (run-jetty handler {:port 1332}))