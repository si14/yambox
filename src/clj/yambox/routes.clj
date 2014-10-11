(ns yambox.routes
  (:require
   [cemerick.friend :as friend]
   [clj-http.client :as http]
   [compojure.core :as c]
   [compojure.route :as route]
   [ring.util.response :as resp]
   [yambox.oauth :as auth]
   [yambox.templates :as tpl]
   [yambox.widget :as widget]))

(c/defroutes main
  (c/GET "/" req
    (if-let [token (auth/req->token req)]
      (resp/redirect "/management")
      (tpl/page-index)))
  (c/GET "/widget/:slug" req (widget/render req))
  (friend/logout (c/GET "/logout" request (resp/redirect "/"))))

(c/defroutes management
  (c/GET "/" req (tpl/page-management req))
  (c/GET "/create-campaign" req (tpl/page-management-create req))

  #_(c/GET "/create-campaign" req
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
           str))))
