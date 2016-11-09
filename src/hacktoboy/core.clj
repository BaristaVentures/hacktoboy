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
  (loop [[{type "type" date "created_at" {name "name"} "repo" {login "login"} "actor"} & t] arr prs '()]
    (if (empty? type)
      (when (> (count prs) 0)
        {:user (:actor (first prs)) :score (count prs) :last (:date (last prs))})
      (recur t (if (and (= type "PullRequestEvent") (.startsWith date "2016-10"))
                 (conj prs {:actor login :repo name :date date})
                 prs)))))

;;Get Members Score
(defn -main [& args]
  (println "User - Score")
  (apply println (map (fn [m] (str "\n" (:user m) " - " (:score m)))
                      (sort-by :score > (filter (fn [x] (> (count x) 0))
                                                (map count-user-pr
                                                     (map parse->clojure (apply members-url-events args))))))))

