(ns yambox.playground
  (:require
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [yambox.schemas :as schemas]
   [yambox.database :as db]
   [yambox.core :as core]))

(defn tmp []
  (db/init! (core/main-config))
  (let [c1 (schemas/strict-map->Campaign
            {:name "foo campaign"
             :slug "foo-campaign"
             :start-money 123
             :target-money 1123
             :created (tc/to-date (t/now))
             :wallet-id 123
             :oauth-token "longstring"})]
    (db/add-campaign c1)
    [(db/get-campaign-by-slug "foo-campaign")
     (db/get-campaign-by-slug "foo-campaign2")]))

#_(tmp)
