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
    [ring.middleware.defaults :as rmd])
  (:gen-class))

(c/defroutes routes
  (c/GET "/" [] "<h1>Hello World Wide Web!</h1>")
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