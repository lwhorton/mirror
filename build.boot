(set-env!
  :source-paths #{"src/clj" "src/cljs"}
  :resource-paths #{"resources"}
  :dependencies '[
                  ;; server
                  [org.clojure/clojure "1.9.0-alpha12"]
                  [org.clojure/data.json "0.2.6"]
                  [compojure "1.6.0-beta3"]
                  ,,,[ring/ring-json "0.4.0"]
                  ,,,[ring/ring-defaults "0.2.3"]
                  [http-kit "2.2.0"]
                  [com.cognitect/transit-clj "0.8.297"]

                  ;; server dev tools
                  [pandeiro/boot-http "0.7.6" :scope "test"]
                  [clj-http "2.2.0" :scope "test"] ;; for some reason boot-cljsjs doesn't work w/o

                  ;; client
                  [org.clojure/clojurescript "1.9.473"]
                  [reagent "0.6.0-rc" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                  ,,,[cljsjs/react-with-addons "15.1.0-0"]
                  ;; boot show -d illuminates that react-dom requires 'react', but we want to use
                  ;; 'react-with-addons', so we will exclude their inclusions
                  ,,,[cljsjs/react-dom "15.1.0-0" :exclusions [cljsjs/react]]
                  ,,,[cljsjs/react-dom-server "15.1.0-0" :exclusions [cljsjs/react]]
                  [re-frame "0.9.1"]
                  [cljsjs/moment "2.17.1-0"]
                  [aft/logging "0.0.1-SNAPSHOT"]
                  [com.cognitect/transit-cljs "0.8.239"]

                  ;; client dev tools
                  [adzerk/boot-cljs "1.7.228-1" :scope "test"] ;; compile cljs -> js
                  [cljsjs/boot-cljsjs "0.5.1" :scope "test"] ;; cljsjs compilation helpers
                  [adzerk/boot-reload "0.4.12" :scope "test"] ;; reload on change
                  [adzerk/boot-cljs-repl "0.3.0" :scope "test"] ;; browser repl
                  ,,,[com.cemerick/piggieback "0.2.1"  :scope "test"]
                  ,,,[weasel                  "0.7.0"  :scope "test"]
                  ,,,[org.clojure/tools.nrepl "0.2.12" :scope "test"]
                  [binaryage/devtools "0.8.3" :scope "test"] ;; chrome cljs devtool enhancements
                  ])

(require
  '[pandeiro.boot-http :refer [serve]]
  '[adzerk.boot-reload :refer [reload]]
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[cljsjs.boot-cljsjs.packaging :refer [download deps-cljs]]
  )

(deftask download-deps
  "Download all libs not pre-packaged in cljsjs, and put them into cljcljs/{dev,prod} folders
  with the correct min.inc.js/inc.js and .deps files."
  []
  (comp
    (download :url "https://cdnjs.cloudflare.com/ajax/libs/skycons/1396634940/skycons.min.js"
              :checksum "aeb228806302953b2dbab9b2e2386e6f")
    (sift :move {#"skycons.min.js" "libs/production/skycons.min.inc.js"})
    (download :url "https://cdnjs.cloudflare.com/ajax/libs/skycons/1396634940/skycons.js"
              :checksum "89abfb1b95efc296a3d67eb0a84ac1fa")
    (sift :move {#"skycons.js" "cljsjs/development/skycons.inc.js"})
    (deps-cljs :name "libs.skycons"
               :no-externs true)
    ))

(deftask dev
  ""
  []
  (comp
    (download-deps)
    (serve :handler 'handler.core/app
           :httpkit true
           :dir "target"
           :reload true)
    (watch)
    (speak)
    (reload)
    (cljs-repl)
    (cljs :compiler-options {:externs ["externs.js"]})
    (target :dir #{"target"})
    ))
