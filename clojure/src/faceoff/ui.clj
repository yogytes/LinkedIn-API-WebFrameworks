;; UI functions
(ns faceoff.ui
  (:use (faceoff li-client)
        (net.cgrand enlive-html)))

;; Profile page
(defn profile-page
  [profile connections]
  ((template "profile.html" []
      [:#firstName] (content (:firstName profile))
      [:#lastName]  (content (:lastName profile))
      [:#headline]  (content (:headline profile))
      [:#summary]   (content (:summary profile))
      [:#picture]   (set-attr :src (:pictureUrl profile))
      [:#connections :li]
        (clone-for [c connections]
           [:h3 [:span (nth-of-type 1)]] (content (:firstName c))
           [:h3 [:span (nth-of-type 2)]] (content (:lastName c))
           [:div [:span (nth-of-type 1)]] (content (:headline c))))))

;; Error page
(defn error-page
  [error]
  ((template "error.html" []
     [:#error] (content error))))

;; Profile handler
(defn profile
  [session]
  (let [token  (:auth-token session)
        [p cs] (with-auth-context token
                [(li-get "http://api.linkedin.com/v1/people/~:(id,first-name,last-name,headline,summary,picture-url)" {:format :json})
                 (li-get "http://api.linkedin.com/v1/people/~/connections" {:format :json})])]
    (if (= (:code p) 200)
      (profile-page (:content p) (:values (:content cs)))
      (error-page (:content p)))))
