(ns animals.repl
  (:require [animals.api :refer [yada-routes]]
            [yada.yada :as yada]
            [bidi.vhosts :refer [vhosts-model]]
            [animals.db :as db]))

(defonce yada-listener (atom nil))

(defn start-app []
  (db/init! db/db)
  (let [port 8090
        vhosts-model (vhosts-model [:* (yada-routes)])
        listener (yada/listener vhosts-model
                                {:port port})]
    (reset! yada-listener listener)
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-app []
  (when-let [listener @yada-listener]
    (if-let [close (:close listener)]
      (close))
    (reset! yada-listener nil)))

(defn reset-app []
  (stop-app)
  (start-app))

;;;; Scratch
(comment
  (reset-app)
  )
