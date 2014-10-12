(ns yambox.database
  (:require
   [clojure.java.io :as io]
   [clojure.java.jdbc :as j]
   [clojure.string :as s]
   [honeysql.core :as sql]
   [honeysql.helpers :as sqlh]
   [plumbing.core :as p]
   [schema.core :as sc]
   [yambox.schemas :as schemas])
  (:import
   [yambox.schemas Campaign]))

(defonce db-spec (atom nil))

;; tracking schema changes in production
(def version ".v1")

(sc/defn init! [config]
  (let [dbfolder (p/safe-get-in config [:db :folder])
        full-path (str dbfolder "/db" version)
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
          [:current_money :bigint]
          [:target_money :bigint]
          [:created :timestamp]
          [:wallet_id :bigint "UNIQUE"]
          [:oauth_token "varchar(300)"]
          [:callback_secret "varchar(50)" "NULL"]
          [:log_salt "char(16)"])))))

;;
;; Utils
;;

(defn to-underscore [m]
  (p/map-keys (fn [k] (-> k name (s/replace "-" "_") keyword)) m))

(defn to-dash [m]
  (p/map-keys (fn [k] (-> k name (s/replace "_" "-") keyword)) m))

(defn queryset-to-campaign [qs]
  (-> qs
      first
      to-dash
      schemas/strict-map->Campaign))

;;
;; Queries
;;

(sc/defn ^:always-validate add-campaign
  [campaign :- Campaign]
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

(sc/defn wallet-id-exists? :- sc/Bool
  [wallet-id :- sc/Int]
  (let [res (j/query @db-spec
              (sql/format {:select [:%count.*]
                           :from [:campaigns]
                           :where [:= :wallet-id wallet-id]}))]
    (-> res first vals first (> 0))))

(sc/defn get-campaign-by-slug :- Campaign
  [slug :- sc/Str]
  (let [res (j/query @db-spec
              (sql/format {:select [:*]
                           :from [:campaigns]
                           :where [:= :slug slug]}))]
    (queryset-to-campaign res)))

(sc/defn get-campaign-by-wallet-id :- Campaign
  [wallet-id :- sc/Int]
  (let [res (j/query @db-spec
              (sql/format {:select [:*]
                           :from [:campaigns]
                           :where [:= :wallet_id wallet-id]}))]
    (queryset-to-campaign res)))

(sc/defn update-campaign
  [campaign :- Campaign]
  (let [changeable? #{:oauth-token :name :current-money}
        fields (->> campaign
                    (filter (fn [[k v]] (changeable? k)))
                    to-underscore)
        wallet-id (:wallet-id campaign)]
    (j/update! @db-spec :campaigns
               fields
               ["wallet_id = ?" wallet-id])))
