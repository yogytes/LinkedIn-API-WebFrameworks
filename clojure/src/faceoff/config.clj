;; Basic config loader
(ns faceoff.config
  (:use [clojure.java.io :only (reader)])
  (:import (java.util Properties)))

(defn- load-properties []
  (with-open [rdr (reader "faceoff.properties")]
    (doto (Properties.)
      (.load rdr))))

(defonce config (load-properties))

(defn prop
  [prop-name]
  (get config prop-name))
