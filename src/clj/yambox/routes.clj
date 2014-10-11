(ns yambox.routes
  (:require
   [clj-http.client :as http]
   [compojure.core :as c]
   [compojure.route :as route]
   [ring.util.response :as resp]
   [yambox.templates :as tpl]))

;;
;; Utils
;;

(defn static-html
  [file-name]
  (-> file-name
      (resp/resource-response {:root "public"})
      (assoc :headers {"Content-Type" "text/html"})))

;;
;; Routes
;;

(c/defroutes main
  (c/GET "/" [] (static-html "index.html"))
  (c/GET "/create" [] (tpl/page-create)))

(c/defroutes secure
  (c/GET "/page" req
    (let [_ (prn "hi")
          token (-> req
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
