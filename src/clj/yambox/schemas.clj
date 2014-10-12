(ns yambox.schemas
  (:require
   [clj-http.client :as http]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [crypto.random :as random]
   [plumbing.core :as p]
   [schema.core :as sc]
   [yambox.oauth :as oauth]))

(sc/defrecord Campaign
    [name :- sc/Str
     slug :- sc/Str
     start-money :- sc/Int
     current-money :- sc/Int
     target-money :- sc/Int
     created :- sc/Inst
     wallet-id :- sc/Int
     oauth-token :- sc/Str
     callback-secret :- (sc/maybe sc/Str)
     log-salt :- sc/Str])

;; not sure if this is the best place for this function
(defn fetch-campaign-data [req campaign]
  (let [oauth-token (oauth/req->token req)
        resp (http/post "https://money.yandex.ru/api/account-info"
                        {:accept :json
                         :oauth-token oauth-token
                         :as :json})
        current-money (-> resp
                          (p/safe-get-in [:body :balance])
                          int)
        log-salt (random/base64 10)
        wallet-id (oauth/req->wallet-id req)
        created (tc/to-date (t/now))]
    (merge campaign
           {:start-money current-money
            :current-money current-money
            :wallet-id wallet-id
            :oauth-token oauth-token
            :created created
            :log-salt log-salt})))

(defn refetch-campaign-data [campaign]
  (let [oauth-token (:oauth-token campaign)
        resp (http/post "https://money.yandex.ru/api/account-info"
                        {:accept :json
                         :oauth-token oauth-token
                         :as :json})
        current-money (-> resp
                          (p/safe-get-in [:body :balance])
                          int)
        log-salt (random/base64 10)
        wallet-id (:wallet-id campaign)
        created (tc/to-date (t/now))]
    (merge campaign
           {:start-money current-money
            :current-money current-money
            :wallet-id wallet-id
            :oauth-token oauth-token
            :created created
            :log-salt log-salt})))
