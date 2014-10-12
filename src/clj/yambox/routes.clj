(ns yambox.routes
  (:require
    [cemerick.friend :as friend]
    [clj-http.client :as http]
    [compojure.core :as c]
    [compojure.route :as route]
    [digest :as d]
    [plumbing.core :as p]
    [schema.coerce :as coerce]
    [ring.util.response :as resp]
    [yambox.oauth :as oauth]
    [yambox.templates :as tpl]
    [yambox.widget :as widget]
    [yambox.database :as db]
    [yambox.schemas :as schemas]
    [slingshot.slingshot :as ss]
    [clj-time.coerce :as tc]
    [clj-time.format :as tf])
  (:import
    [yambox.schemas Campaign]))

;;
;; Utils
;;

(def parse-campaign (coerce/coercer Campaign coerce/string-coercion-matcher))

(defn mask-wallet
  [salt x]
  (update-in x [:title] (fn [t] (clojure.string/replace t #"[\d]{5,15}"
                                  (fn [r]
                                    (let [masked (-> r
                                                     (str salt)
                                                     d/sha-256
                                                     (subs 0 16))]
                                      masked))))))

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

(defn get-campaign-page
  [req]
  (let [slug (p/safe-get-in req [:params :slug])]
    (if-not (db/slug-exists? slug)
      (route/not-found "Page not found")
      (let [campaign (db/get-campaign-by-slug slug)
            token (:oauth-token campaign)
            resp (http/post
                  "https://money.yandex.ru/api/operation-history"
                  {:accept      :json
                   :form-params {:records 100
                                 :type "deposition payment"}
                   :oauth-token token
                   :as          :json})
            operations (->> resp
                            :body
                            :operations
                            (filter #(= (:status %) "success"))
                            (map
                              (fn [x]
                                (if (= (:direction x) "out")
                                  (update-in x [:amount] #(* % -1))
                                  x)))
                            (map (partial mask-wallet (:log-salt campaign))))]
        (if (> (count operations) 0)
          (tpl/page-campaign req operations campaign (oauth/req->token req))
          (tpl/page-campaign-empty req campaign))))))

(defn get-widget-page
  [req]
  (let [slug (p/safe-get-in req [:params :slug])]
    (if-not (db/slug-exists? slug)
      (widget/render-empty)
      (let [campaign (db/get-campaign-by-slug slug)]
        (widget/render campaign)))))

;;
;; Routes
;;

(c/defroutes main
  (c/GET "/" req
    (if (oauth/req->token req)
      (resp/redirect "/management")
      (tpl/page-index)))
  (c/GET "/campaigns/:slug" req (get-campaign-page req))
  (c/GET "/campaigns/:slug/widget" req (get-widget-page req))
  (friend/logout (c/GET "/logout" request (resp/redirect "/"))))

(c/defroutes management
  (c/GET "/" req
   (let [wallet-id (oauth/req->wallet-id req)]
     (if (db/wallet-id-exists? wallet-id)
       (do
         (handle-campaign-change false req)
         (tpl/page-management req (db/get-campaign-by-wallet-id wallet-id)))
       (tpl/page-management-create req))))
  (c/POST "/" req
    (let [create? (not (db/wallet-id-exists? (oauth/req->wallet-id req)))]
      (handle-campaign-change create? req)
      (resp/redirect "/management"))))
