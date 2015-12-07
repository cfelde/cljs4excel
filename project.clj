(defproject cljs4excel "0.2.1-SNAPSHOT"
  :description "ClojureScript REPL within Microsoft Excel"

  :url "https://www.cljs4excel.com"

  :license {:name "Eclipse Public License - v 1.0"
            :url "https://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [cljsjs/jqconsole "2.13.1-0"]
                 [replumb "0.1.2"]]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-codox "0.9.0"]
            [lein-resource "15.10.1"]]

  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler {:output-to "target/cljs4excel.js"  ; default: target/cljsbuild-main.js
                                    :output-dir "target"
                                    :asset-path "target"
                                    :optimizations :none
                                    :pretty-print true}}]}

  :prep-tasks ["codox"]

  :codox {:language :clojurescript
          :source-paths ["src"]
          :namespaces [cljs4excel.core]
          :output-path "doc"
          :metadata {:doc/format :markdown}}

  :resource {
              :resource-paths [["src-resource"
                                  {
                                    :includes [ #".*"]
                                    :excludes []
                                    :target-path "target"}]]
              :target-path "target"
              :update false
              :includes [ #".*"]
              :excludes [ #".*~"]
              :silent false
              :verbose false
              :skip-stencil [ #".*"]})
