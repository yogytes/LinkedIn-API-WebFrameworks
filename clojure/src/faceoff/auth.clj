;; Basic Linkedin OAuth authentication
(ns faceoff.auth
  (:use (faceoff config li-client)
        (ring.util response)))
  
(defn authenticated
  [{session :session}]
  (boolean (:auth-token session)))

(defn authenticate
  [req]
  (let [{session :session}     req
        {port    :server-port} req
        {scheme  :scheme}      req
        {params  :params}      req
        {token :oauth_token verifier :oauth_verifier error :oauth_problem} params
        url (str (name scheme) "://" (prop "host.url") ":" port)]
    (cond
      error
        (response (str "Authentication error " error))
      (and token verifier)
        (do
          (authenticate-context token verifier)
          (-> (redirect url)
            (assoc :session (assoc session :auth-token token))))
      :else
        (let [ctx (create-auth-context (prop "oauth.api.key") (prop "oauth.secret.key") url)]
          (redirect (approval-uri ctx))))))
