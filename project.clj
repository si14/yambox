(defproject yambox "0.1.0-SNAPSHOT"
  :description "Yandex.Money Hackathon 2014 project"
  :url "http://yambox.org"
  :license {:name "Eclipse Public License 1.0 (EPL-1.0)"
            :url  "https://tldrlegal.com/license/eclipse-public-license-1.0-(epl-1.0)"}
  :plugins [[lein-ring "0.8.12"]
            [lein-cljsbuild "1.0.3"]
            [lein-asset-minifier "0.2.0"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2356"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [ring/ring "1.3.1" :exclusions [org.clojure/java.classpath]]
                 [ring/ring-defaults "0.1.2"]
                 [compojure "1.2.0"]
                 [com.cemerick/friend "0.2.1"]
                 [jarohen/nomad "0.7.0"]
                 [prismatic/plumbing "0.3.3"]
                 [clj-redis-session "2.1.0"]

                 ;; friend-oauth2 deps
                 [clj-http "0.7.7"]
                 [cheshire "5.2.0"]
                 [crypto-random "1.1.0"]]
  :source-paths ["src/clj"]
  :main ^:skip-aot yambox.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :jvm-opts ["-Xmx512M"]
  :ring {:handler      yambox.core/handler
         :reload-paths ["src/clj"]}
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs"]
                        :compiler     {:output-to     "resources/public/js/yambox_dev.js"
                                       :output-dir    "resources/public/js/out_dev"
                                       :optimizations :none
                                       :source-map    true}}
                       {:id           "release"
                        :source-paths ["src/cljs"]
                        :compiler     {:output-to     "resources/public/js/yambox.js"
                                       :output-dir    "resources/public/js/out"
                                       :optimizations :advanced}}]}
  :minify-assets {:assets {"resources/public/yambox.css"
                            "resources/public/css/"}})
