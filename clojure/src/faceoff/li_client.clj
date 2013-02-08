;; Basic Linkedin API client
(ns faceoff.li-client
  (:require [oauth.client :as oauth]
            [com.twinql.clojure.http :as http]))

(def *auth-context-store* (atom {}))

(defn- consumer
  [api-key secret-key]
  (oauth/make-consumer api-key
                       secret-key
                       "https://api.linkedin.com/uas/oauth/requestToken"
                       "https://api.linkedin.com/uas/oauth/accessToken"
                       "https://www.linkedin.com/uas/oauth/authorize"
                       :hmac-sha1))

(defrecord LiOAuthContext [consumer
                           request-token
                           access-token
                           access-secret])

(defn create-auth-context
  [api-key secret-key auth-callback]
  (let [consumer      (consumer api-key secret-key)
        request-token (oauth/request-token consumer auth-callback)
        context       (LiOAuthContext. consumer request-token nil nil)]
    (swap! *auth-context-store* assoc (:oauth_token request-token) context)
    context))

(defn get-context
  [request-token]
  (@*auth-context-store* request-token))

(defn remove-context
  [request-token]
  (swap! *auth-context-store* dissoc request-token))

(defn approval-uri
  [#^LiOAuthContext context]
  (oauth/user-approval-uri (:consumer context) (:oauth_token (:request-token context))))
 
(defn authenticate-context
  [token verifier]
  (let [context    (get-context token)
        auth-token (oauth/access-token (:consumer context) (:request-token context) verifier)
        context    (assoc context :access-token (:oauth_token auth-token) :access-secret (:oauth_token_secret auth-token))]
    (swap! *auth-context-store* assoc (:oauth_token (:request-token context)) context)
    context))

(def ^:dynamic *consumer* nil)
(def ^:dynamic *access-token* nil)
(def ^:dynamic *access-secret* nil)

(defmacro with-auth-context
  [token & body]
  `(let [ctx#  (get-context ~token)]
     (binding [*consumer*      (:consumer ctx#)
               *access-token*  (:access-token ctx#)
               *access-secret* (:access-secret ctx#)]
       (do
         ~@body))))

(defn li-get
  [uri & [params]]
  (let [params      (or params {})
        credentials (oauth/credentials *consumer* *access-token* *access-secret* :GET uri params)
        result-type (case (:format params) 
                      :json :json
                      :string)
        resp        (http/get uri 
                              :query (merge credentials params)
                              :parameters (http/map->params {:use-expect-continue false})
                              :as result-type)]
      resp))

