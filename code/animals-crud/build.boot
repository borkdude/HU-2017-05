(def version "0.1.0-SNAPSHOT")

(set-env!
 :source-paths #{"src" "src-cljs"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.521"]
                 [org.clojure/core.async "0.3.442"]
                 [org.webjars/bootstrap "3.3.6"]
                 [cljs-http "0.1.43"]
                 [reagent "0.6.1"]
                 [prismatic/schema "1.1.5"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [com.h2database/h2 "1.4.195"]
                 [yada "1.2.2"]
                 [aleph "0.4.3"]
                 [bidi "2.0.17"]
                 ;; dev dependencies
                 [adzerk/boot-cljs "2.0.0" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                 [com.cemerick/piggieback "0.2.1"  :scope "test"]
                 [weasel                  "0.7.0"  :scope "test"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [adzerk/boot-reload "0.5.1" :scope "test"]
                 [binaryage/devtools "0.9.2" :scope "test"]])

(require '[animals.repl])

(deftask start-app []
  (with-pass-thru [_]
    (animals.repl/start-app)))

(require '[adzerk.boot-cljs :refer :all])
(require '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])
(require '[adzerk.boot-reload :refer [reload]])

(deftask dev []
  (comp
   (start-app)
   (repl :init-ns 'animals.repl)
   (watch)
   (reload :asset-path "/public")
   (cljs-repl)
   (cljs
    :optimizations :none
    :compiler-options {:preloads '[devtools.preload]})
   ))
