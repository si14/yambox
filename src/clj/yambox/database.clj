(ns yambox.database
  (:require
   [clojure.java.io :as io]
   [clojure.java.jdbc :as j]
   [clojure.string :as s]
   [honeysql.core :as sql]
   [plumbing.core :as p]
   [schema.core :as sc]
   [yambox.schemas :as schemas])
  (:import
   [yambox.schemas Campaign]))

(defonce db-spec (atom nil))

;; tracking schema changes in production
(def version ".v1")

(sc/defn init! [settings]
  (let [subfolder (p/safe-get-in settings [:db :subfolder])
        full-path (str (System/getProperty "user.dir") "/"
                       subfolder "/db"
                       version)
        spec {:classname   "org.h2.Driver"
              :subprotocol "h2:file"
              :subname     full-path
              :user        "sa"
              :password    ""}]
    (reset! db-spec spec)
    (when-not (.exists (io/as-file (str full-path ".mv.db")))
      (j/db-do-commands spec
        (j/create-table-ddl :campaigns
          [:name "varchar(140)"]
          [:slug "varchar(140)" "PRIMARY KEY"]
          [:start_money :bigint]
          [:target_money :bigint]
          [:created :timestamp]
          [:wallet_id :bigint]
          [:oauth_token "varchar(300)"]))
      (j/db-do-commands spec
        "CREATE INDEX campaigns_slug_ix ON campaigns(slug)")
      (j/db-do-commands spec
        "CREATE INDEX campaigns_wallet_id_ix ON campaigns(wallet_id)"))))

(defn to-underscore [m]
  (p/map-keys (fn [k] (-> k name (s/replace "-" "_") keyword)) m))

(sc/defn ^:always-validate add-campaign
  [campaign :- Campaign]
  (prn (to-underscore campaign))
  (j/insert! @db-spec
    :campaigns
    (to-underscore campaign)))

(sc/defn slug-exists? :- sc/Bool
  [slug :- sc/Str]
  (let [res (j/query @db-spec
              (sql/format {:select [:%count.*]
                           :from [:campaigns]
                           :where [:= :slug slug]}))]
    (-> res first vals first (> 0))))

(sc/defn get-campaign-by-slug :- Campaign
  [slug :- sc/Str]
  (j/query @db-spec
    (sql/format {:select [:*]
                 :from [:campaigns]
                 :where [:= :slug slug]})))

(sc/defn get-campaign-by-wallet-id :- Campaign
  [wallet-id :- sc/Int]
  (j/query @db-spec
    (sql/format {:select [:*]
                 :from [:campaigns]
                 :where [:= :wallet_id wallet-id]})))
