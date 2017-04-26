;; Temporary patch until https://github.com/juxt/yada/pull/167 is pulled and released

(ns yada.resources.classpath-resource
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [yada.resource :refer [as-resource resource]]))

(defn new-classpath-resource
  "Create a new classpath resource that resolves requests with
   path info in the active classpath, relative to root-path.

   Optionally takes an options map with the following keys:

   * index-files - a collection of files to try if the path info
                   ends with /

   Example:

      (new-classpath-resource \"public\")

   would resolve index.html to public/index.html inside
   the active classpath (e.g. of the JAR that serves the resource).

   If used with bidi, the following route

     [\"\" (yada (new-classpath-resource
                \"public\" {:index-files [\"index.html\"]}))]

   can be used to serve all files in public/ and fall back to
   index.html for URL paths like / or foo/."
  ([root-path]
   (new-classpath-resource root-path nil))
  ([root-path {:keys [index-files skip-dir?]}]
   (resource
    {:path-info?   true
     :methods      {}
     :sub-resource
     (fn [ctx]
       (let [path-info (-> ctx :request :path-info)
             path      (str/replace path-info #"^/" "")
             files     (if (= (last path-info) \/)
                         (map #(io/file root-path path %) index-files)
                         (list (io/file root-path path)))
             transducers (cond-> [(map #(.getPath ^java.io.File %))
                                 (map io/resource)
                                 (drop-while nil?)]
                          skip-dir? (conj (remove #(.isDirectory (io/file %)))))
             res       (first (sequence (apply comp
                                         transducers)
                                        files))]
         (as-resource res)))})))
