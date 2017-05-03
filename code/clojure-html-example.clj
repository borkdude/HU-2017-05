(require '[hiccup.core :as html])
(require '[hiccup.page :as page])

(defn numbers [n]
  (page/html5
   [:head
    [:title "Natural numbers"]]
   [:body
    [:p "A list of natural numbers:"]
    [:ul
     (for [i (range 10)]
       [:l i])]]))

(defn -main [& args]
  (println (numbers 10)))
