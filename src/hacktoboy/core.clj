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

(defn members-url-events [org-name]
  "Get Url Events of Public Members  for org-name"
  (map (partial get-value "events_url") (parse->clojure (str base-url "/orgs/" org-name "/members"))))

(defn get-pullrequest [event]
  (let [type (get event "type")
        actor (get-in event ["actor" "login"])
        repo (get-in event ["repo" "name"])
        date (get event "created_at")]
    (if (= type "PullRequestEvent")
      (str actor " - " type " - " repo " - " date))))

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

;;Get all PullRequestEvents of Barista Ventures Public Members
(map clean-arr (map (fn [arr] (map print-event arr)) (map parse->clojure (members-url-events "BaristaVentures"))))
