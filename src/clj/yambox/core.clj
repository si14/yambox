(ns yambox.core
  (:require
   [clojure.java.io :as io]
   [plumbing.core :as p]
   [cemerick.friend :as friend]
   [compojure.core :as c]
   [compojure.route :as cr]
   [hiccup.core :as h]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.anti-forgery :as raf]
   [ring.middleware.defaults :as rmd]
   [clj-redis-session.core :refer [redis-store]]
   [nomad :as nom :refer [defconfig]]
   [nomad.map :refer [deep-merge]]
   [yambox.oauth :as oauth]
   [yambox.routes :as routes])
  (:gen-class))

;;
;; State
;;

(defconfig main-config
  (io/resource "main-config.edn"))

(defonce server (atom nil))

;;
;; Root handler
;;

(defn get-handler [config]
  (let [session-store (redis-store {:pool {} :spec {}})
        middleware-conf (deep-merge rmd/site-defaults
                                    {:security {:anti-forgery false}}
                                    #_{:session {:store session-store}})
        naked-handler (c/routes
                       routes/main
                       (c/context "/management" req
                         (friend/wrap-authorize routes/secure #{::user}))
                       (cr/resources "/")
                       (cr/not-found "Page not found"))]
    (-> naked-handler
        (friend/authenticate (oauth/friend-config config))
        (rmd/wrap-defaults middleware-conf))))

;;
;; Lifecycle
;;

(defn start
  ([] (start false))
  ([join?]
     (let [config (main-config)
           port (p/safe-get config :port)
           handler (get-handler config)
           jetty-config {:port port
                         :join? join?}
           new-server (run-jetty handler jetty-config)]
       (reset! server new-server))))

(defn stop []
  (.stop @server))

(defn restart []
  (when @server
    (stop))
  (start))

(defn -main
  [& args]
  (start true))
