(ns yambox.routes
  (:require
   [cemerick.friend :as friend]
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
  (c/GET "/create" [] (tpl/page-create))
  (friend/logout (c/GET "/logout" request (resp/redirect "/"))))

(c/defroutes secure
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
