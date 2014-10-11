(ns yambox.schemas
  (:require
   [schema.core :as sc]))

(sc/defrecord Campaign
    [name :- sc/Str
     slug :- sc/Str
     start-money :- sc/Int
     target-money :- sc/Int
     created :- sc/Inst
     wallet-id :- sc/Int
     oauth-token :- sc/Str])
