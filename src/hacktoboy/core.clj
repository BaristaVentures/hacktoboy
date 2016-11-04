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
                          

(defn get-pullrequest [event]
  (let [type (get event "type")
        actor (get-in event ["actor" "login"])
        repo (get-in event ["repo" "name"])
        action (get-in event ["payload" "action"])
        date (get event "created_at")]
    (if (= type "PullRequestEvent")
      (if (.startsWith date "2016-10")
        (hash-map :actor actor :repo repo :date date)))))

(defn clean-arr [arr]
  (loop [init arr
         final '()]
    (if (empty? init)
      final
      (let [[element & remaining] init]
        (recur remaining
               (if (= element nil)
                 final
                 (conj final element)))))))

(defn count-pr [arr-pr]
  (if (> (count arr-pr) 0)
    {:user (get (first arr-pr) :actor) :score (count arr-pr) :lastpr (get (first arr-pr) :date)}))

;;Get Members Score
(sort-by :score > (filter (fn [x] (> (count x) 0)) (map count-pr (map clean-arr (map (fn [arr] (map get-pullrequest arr))
    (map parse->clojure (members-url-events "BaristaVentures")))))))
