(ns yambox.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as s]
   [plumbing.core :as p]
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
   [friend-oauth2.workflow :as oauth2]
   [friend-oauth2.util :refer [format-config-uri]]
   [yambox.templates :as tpl]
   [nomad :as nom :refer [defconfig]])
  (:gen-class))

(defconfig main-config
  (io/resource "main-config.edn"))

(defn credential-fn
  [token]
  {:identity token
   :roles #{::user}})

(defn get-uri-config []
  (let [client-config (p/safe-get (main-config) :oauth)
        required-rights-str (->> :required-rights
                                 (p/safe-get client-config)
                                 (map name)
                                 (s/join " "))]
    {:authentication-uri {:url "https://sp-money.yandex.ru/oauth/authorize"
                          :query {:client_id (:client-id client-config)
                                  :response_type "code"
                                  :redirect_uri (format-config-uri client-config)
                                  :scope required-rights-str}}

     :access-token-uri {:url "https://sp-money.yandex.ru/oauth/token"
                        :query {:client_id (:client-id client-config)
                                :client_secret (:client-secret client-config)
                                :grant_type "authorization_code"
                                :redirect_uri (format-config-uri client-config)}}}))

(def friend-config
  {:allow-anon? true
   :workflows [(oauth2/workflow
                {:client-config (p/safe-get (main-config) :oauth)
                 :uri-config (get-uri-config)
                 :credential-fn credential-fn})]})

(c/defroutes secure-routes
  (c/GET "/page" req
    (let [token (-> req
                    :session
                    :cemerick.friend/identity
                    :current
                    :access-token)
          resp (http/post "https://money.yandex.ru/api/account-info"
                      {:accept :json
                       :oauth-token token
                       :as :json})]
      (->> resp
           :body
           :balance
           str))))

(defn static-html
  [file-name]
  (-> file-name
      (resp/resource-response {:root "public"})
      (assoc :headers {"Content-Type" "text/html"})))

(c/defroutes routes
  (c/GET "/" [] (static-html "index.html"))
  (c/GET "/create" [] (tpl/page-create))
  (route/resources "/"))

(def handler
  (->
   (c/routes
    routes
    (c/context "/secure" req
               (friend/wrap-authorize secure-routes #{::user}))
    (route/not-found "Page not found"))
   (friend/authenticate friend-config)
   (rmd/wrap-defaults (-> rmd/site-defaults
                          (assoc-in [:security :anti-forgery] false)))))

(defonce server (atom nil))

(defn start []
  (let [port (p/safe-get (main-config) :port)
        new-server (run-jetty handler {:port port
                                       :join? false})]
    (reset! server new-server)))

(defn stop []
  (.stop @server))

(defn restart []
  (when @server
    (stop))
  (start))

(defn -main
  [& args]
  (let [port (p/safe-get (main-config) :port)]
    (run-jetty handler {:port port})))
