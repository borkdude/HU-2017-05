(ns animals.repl
  (:require [ring.server.standalone :refer [serve]]
            [animals.api :refer [handler init yada-routes]]
            [yada.yada :as yada]
            [bidi.vhosts :refer [vhosts-model]]))

(defonce server (atom nil))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 8080)]
    (reset! server
            (serve #'handler
                   {:port port
                    :init init
                    :auto-reload? true                    
                    :join true
                    :open-browser? false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))

(defonce yada-listener (atom nil))

(comment
  (reset-yada))

(defn start-yada []
  (let [vhosts-model (vhosts-model [:* (yada-routes)])
        listener (yada/listener vhosts-model
                                {:port 8090})]
    (reset! yada-listener listener)
    (println "Started yada")))

(defn stop-yada []
  (when-let [listener @yada-listener]
    (if-let [close (:close listener)]
      (close))
    (reset! yada-listener nil)))

(defn reset-yada []
  (stop-yada)
  (start-yada))

;;;; Scratch
(comment
  (reset-yada)
  )
