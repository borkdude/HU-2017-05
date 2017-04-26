(ns animals.repl
  (:require [animals.api :refer [init yada-routes]]
            [yada.yada :as yada]
            [bidi.vhosts :refer [vhosts-model]]
            [integrant.core :as ig]
            [animals.db :as db]))

(def config
  {:yada {:port 9000}})

(defmethod ig/init-key :yada [_ {:keys [port database] :as opts}]
  (let [vhosts-model (vhosts-model [:* (yada-routes)])
        listener (yada/listener vhosts-model
                                {:port port})]
    (println "You can view the site at http://localhost:" port)
    listener))

(defmethod ig/halt-key! :adapter/jetty [_ listener]
  (if-let [close (:close listener)]
    (close)))

(defn start-system []
  (ig/init config))

(defonce yada-listener (atom nil))

(defn start-server []
  (let [port 8090
        vhosts-model (vhosts-model [:* (yada-routes)])
        listener (yada/listener vhosts-model
                                {:port port})]
    (reset! yada-listener listener)
    (println "You can view the site at http://localhost:" port)))

(defn stop-server []
  (when-let [listener @yada-listener]
    (if-let [close (:close listener)]
      (close))
    (reset! yada-listener nil)))

(defn reset-server []
  (stop-server)
  (start-server))

;;;; Scratch
(comment
  (reset-server)
  (start-system)
  )
