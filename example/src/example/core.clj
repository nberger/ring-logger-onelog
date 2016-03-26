(ns example.core
  (:require [clj-http.client :as http]
            clj-http.cookies
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.adapter.jetty :as jetty]
            [onelog.core :as onelog]
            [ring.logger.onelog :as logger]))

(defroutes handler
  (GET "/" [name] {:body (format "<h1>Hello, %s!</h1>" name)
                   :status 200
                   :cookies {"password" {:value "007"}
                             "user" {:value "you"}} })
  (POST "/throws" [] (throw (Exception. "Oops, sooooorry")))
  (route/not-found "<h1>Page not found</h1>"))

(defn app [make-logger-fn]
  (-> handler
      wrap-cookies
      make-logger-fn
      wrap-keyword-params
      wrap-nested-params
      wrap-params))

(defn run [make-logger-fn]
  (onelog/start!)
  (let [server (jetty/run-jetty (app make-logger-fn)
                                {:port 14587
                                 :join? false})
        cookie-store (clj-http.cookies/cookie-store)]

    ; Hello ring-logger!
    (http/get "http://localhost:14587/?name=ring-logger"
              {:headers {"foo" "baz"
                         "AuThorization" "Basic super-secret!"}
               :cookie-store cookie-store})

    ; Hello Nico!
    (http/get "http://localhost:14587/?name=Nico&password=pass"
              {:headers {"foo" "baz"
                         "AuThorization" "Basic super-secret!"}
               :cookie-store cookie-store})

    ; not found
    (try
      (http/get "http://localhost:14587/not-found")
      ; ignore
      (catch Throwable t))

    ; throws
    (try
      (http/post "http://localhost:14587/throws" {:form-params {:foo "bar"
                                                                :nested {:password "5678"
                                                                         :id 1}
                                                                :password "1234"}})
      ; ignore
      (catch Throwable t))

    (println "Done. See that awesome log. I'm stopping the server now...")

    ; stop server
    (.stop server)))

(defn -main [& args]
  (run logger/wrap-with-logger)
  (run #(logger/wrap-with-logger % {:printer :no-color})))
