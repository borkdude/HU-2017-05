(ns animals.api
  (:require
   [animals.animals :as animals]
   [animals.db :as db]
   [yada.yada :as yada]
   [yada.resources.classpath-resource :as cp]
   [yada.resources.webjar-resource :as wj]
   [yada.context :as ctx]
   [schema.core :as s]))

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
    ["/assets/jquery" (wj/new-webjar-resource "jquery")]
    ["/assets/bootstrap" (wj/new-webjar-resource "bootstrap")]
    ["" (cp/new-classpath-resource "public" {:index-files ["index.html"]
                                             :skip-dir? true})]]])

(defn init
  []
  (println "initializing application")
  (animals/init db/db))
