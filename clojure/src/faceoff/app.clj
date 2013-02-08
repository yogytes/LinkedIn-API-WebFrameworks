;; Application
(ns faceoff.app
  (:use (faceoff auth ui)
        (compojure core route)
        (ring.middleware session params keyword-params)))

;; Route definition
(defroutes app-routes
  (GET "/" {session :session} (profile session))
  (resources "/public")
  (not-found "Page not found"))

;; Authentication handler
(defn wrap-auth
  [handler]
  (fn [req]
    (if (authenticated req)
      (handler req)
      (authenticate req))))

;; Application hander
(def run
  (-> app-routes
     wrap-auth
     wrap-session
     wrap-keyword-params
     wrap-params))
