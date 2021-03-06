(ns yambox.oauth
  (:require
   [clojure.string :as s]
   [clj-http.client :as http]
   [friend-oauth2.workflow :as oauth2]
   [friend-oauth2.util :refer [format-config-uri]]
   [plumbing.core :as p]))

(defn credential-fn
  [token]
  (let [naked-token (p/safe-get token :access-token)
        resp (http/post "https://money.yandex.ru/api/account-info"
                        {:accept :json
                         :oauth-token naked-token
                         :as :json})
        wallet-id (Long/parseLong (p/safe-get-in resp [:body :account]))]
    {:identity [naked-token wallet-id]
     :roles #{::user}}))

(defn req->token [req]
  (first
   (get-in req [:session :cemerick.friend/identity :current])))

(defn req->wallet-id [req]
  (second
   (get-in req [:session :cemerick.friend/identity :current])))

(p/defnk format-rights [required-rights]
  (s/join " " (map name required-rights)))

(p/defnk get-uri-config [client-id client-secret :as oauth]
  {:authentication-uri {:url "https://sp-money.yandex.ru/oauth/authorize"
                        :query {:client_id client-id
                                :response_type "code"
                                :redirect_uri (format-config-uri oauth)
                                :scope (format-rights oauth)}}

   :access-token-uri {:url "https://sp-money.yandex.ru/oauth/token"
                      :query {:client_id client-id
                              :client_secret client-secret
                              :grant_type "authorization_code"
                              :redirect_uri (format-config-uri oauth)}}})

(p/defnk friend-config [oauth]
  {:allow-anon? true
   :workflows [(oauth2/workflow
                {:client-config oauth
                 :uri-config (get-uri-config oauth)
                 :credential-fn credential-fn})]})
