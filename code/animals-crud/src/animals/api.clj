(ns animals.api
  (:require
   [liberator.core :refer (resource)]
   [compojure.core :refer (defroutes ANY GET)]
   [compojure.route :refer (resources not-found)]
   [ring.middleware.params :refer (wrap-params)]
   [ring.middleware.edn :refer (wrap-edn-params)]
   [ring.util.response :refer (redirect)]
   [animals.animals :as animals]
   [clj-json.core :as json]
   [animals.db :as db]
   [clojure.edn :as edn]
   [yada.yada :as yada]
   [yada.context :as ctx]
   [schema.core :as s]))

(defn handle-exception
  [ctx]
  (let [e (:exception ctx)]
    (.printStackTrace e)
    {:status 500 :message (.getMessage e)}))

(defroutes routes
  (GET "/greeting" []
       "Hello World!")
  (ANY "/"
       []
       (redirect "/index.html"))

  (resources "/" {:root "public"})
  (resources "/" {:root "/META-INF/resources"})
  (not-found "404"))

(def formats #{"application/edn" "application/json"})

(defn new-animals-resource []
  (yada/resource
   {:consumes formats
    :produces formats
    :methods
    {:get {:response (fn [ctx]
                       (animals/read db/db))}
     :post
     {:parameters {:body {:name s/Str
                          :species s/Str}}
      :response
      (fn [ctx]
        (animals/create! db/db
                         (-> ctx :parameters :body)))}}}))

(defn new-animal-resource []
  (yada/resource
   {:consumes formats
    :produces formats
    :parameters {:path {:id s/Int}}
    :methods
    {:get
     {:response
      (fn [ctx]
        (let [id (ctx/path-parameter ctx :id)]
          (animals/read db/db id)))}
     :put
     {:parameters {:body {:name s/Str
                          :species s/Str}}
      :response
      (fn [ctx]
        (let [id (ctx/path-parameter ctx :id)]
          (animals/update!
           db/db id
           (-> ctx :parameters :body))))}
     :delete (fn [ctx]
               (let [id (ctx/path-parameter ctx :id)]
                 (let [res (animals/delete! db/db id)]
                   (println res)
                   {:ref res})))}}))

(defn yada-api-routes []
  [""
   [["/animals" (new-animals-resource)]
    [["/animals/" :id] (new-animal-resource)]]])

(defn yada-routes []
  ;; A branch is a vector of a fragment + vector with leafs or branches
  ;; A leaf is a tuple of a fragment + handler/resource
  
  ["" ;; branch
   ;; branch 
   [(yada-api-routes)
    ["/" (fn [req] (redirect "index.html"))]
    [true handler]]])

(def handler
  (-> routes
      wrap-params
      wrap-edn-params))

(defn init
  []
  (println "initializing application")
  (animals/init db/db))
