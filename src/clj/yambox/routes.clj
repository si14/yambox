(ns yambox.routes
  (:require
   [cemerick.friend :as friend]
   [clj-http.client :as http]
   [compojure.core :as c]
   [compojure.route :as route]
   [plumbing.core :as p]
   [schema.coerce :as coerce]
   [ring.util.response :as resp]
   [yambox.oauth :as oauth]
   [yambox.templates :as tpl]
   [yambox.widget :as widget]
   [yambox.database :as db]
   [yambox.schemas :as schemas])
  (:import
   [yambox.schemas Campaign]))

;;
;; Utils
;;

(def parse-campaign (coerce/coercer Campaign coerce/string-coercion-matcher))

;;
;; Handler functions
;;

(defn handle-campaign-change [create? req]
  (let [wallet-id (oauth/req->wallet-id req)
        known (if create? {} (db/get-campaign-by-wallet-id wallet-id))
        campaign (->> (:params req)
                      (merge known)
                      (schemas/fetch-campaign-data req)
                      schemas/map->Campaign
                      parse-campaign)]
    (if create?
      (db/add-campaign campaign)
      (db/update-campaign campaign))))

;;
;; Routes
;;

(c/defroutes main
  (c/GET "/" req
    (if (oauth/req->token req)
      (resp/redirect "/management")
      (tpl/page-index)))
  (c/GET "/campaign/:slug" req (tpl/page-campaign req))
  (c/GET "/campaign/:slug/widget" req (widget/render req))
  (friend/logout (c/GET "/logout" request (resp/redirect "/"))))

(c/defroutes management
  (c/GET "/" req
   (let [wallet-id (oauth/req->wallet-id req)]
     (if (db/wallet-id-exists? wallet-id)
       (tpl/page-management req (db/get-campaign-by-wallet-id wallet-id))
       (tpl/page-management-create req))))
  (c/POST "/" req
    (let [create? (not (db/wallet-id-exists? (oauth/req->wallet-id req)))]
      (handle-campaign-change create? req)
      (resp/redirect "/management"))))
