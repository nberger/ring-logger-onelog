(defproject ring-logger-onelog "0.7.7-SNAPSHOT"
  :description "OneLog implementation for ring-logger"
  :url "http://github.com/nberger/ring-logger-onelog"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring-logger "0.7.5"]
                 [onelog "0.4.5"]]
  :profiles {:dev {:dependencies [[ring/ring-mock "0.2.0"]]}})
