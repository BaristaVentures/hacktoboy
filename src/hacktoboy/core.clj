(ns hacktoboy.core
  (:require [cheshire.core :refer :all]))

(def base-url "https://api.github.com")

(defn parse->clojure [url]
  "Parse Json to Clojure"
  (parse-string (slurp url)))

(defn getValue [key user]
  "Get the value given a key"
  (user key))

;Get Url Events of Barista Public Members
(map (partial getValue "events_url") (parse->clojure (str base-url "/orgs/BaristaVentures/members")))
