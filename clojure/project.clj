(defproject clj-face-off "0.0.1-SNAPSHOT"
    :dependencies [[org.clojure/clojure	"1.2.1"]
                   [compojure "1.0.1"]
                   [enlive "1.0.0"]
                   [clj-oauth "1.3.1-SNAPSHOT"]
                   [org.clojars.tavisrudd/clj-apache-http "2.3.2-SNAPSHOT"]]
  :dev-dependencies [[lein-ring "0.6.1"]
                     [ring-server "0.2.1"]]
  :ring {:handler faceoff.app/run})
