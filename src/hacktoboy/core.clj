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

;;Get Url Events of Barista Public Members
(defn members-url-events [org-name]
  (map (partial get-value "events_url") (parse->clojure (str base-url "/orgs/" org-name "/members"))))

(members-url-events "BaristaVentures")
