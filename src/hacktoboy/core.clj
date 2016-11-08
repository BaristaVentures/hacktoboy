(ns hacktoboy.core
  (:require [cheshire.core :refer :all]))

(def base-url "https://api.github.com")

(defn parse->clojure [url]
  "Parse Json to Clojure"
  (parse-string (slurp url)))

(defn get-value [key user]
  "Get the value given a key"
  (let [s (user key)]
    (subs s 0 (- (.length s) (.length "{/privacy}")))))

(defn members-url-events
  ([org-name]
    "Get Url Events of Public Members  for org-name"
      (map (partial get-value "events_url") (parse->clojure (str base-url "/orgs/" org-name "/members"))))
  ([org-name token]
    "Get Url Events for all Member for org-ame. Requires token and be member"
      (map (partial get-value "events_url") (parse->clojure (str base-url "/orgs/" org-name "/members?access_token=" token)))))
                    
(defn count-user-pr [arr]
  (loop [[h & t] arr prs '()]
    (if (empty? h)
      (if (> (count prs) 0)
        {:user (:actor (first prs)) :score (count prs) :last (:date (last prs))})
      (recur t (if (and (= (get h "type") "PullRequestEvent") (.startsWith (get h "created_at") "2016-10"))
                 (conj prs {:actor (get-in h ["actor" "login"]) :repo (get-in h ["repo" "name"]) :date (get h "created_at")})
                 prs)))))

;;Get Members Score
(defn -main [& args]
  (println "User - Score")
  (apply println (map (fn [m] (str "\n" (:user m) " - " (:score m)))
                      (sort-by :score > (filter (fn [x] (> (count x) 0))
                                                (map count-user-pr
                                                     (map parse->clojure (apply members-url-events args))))))))

