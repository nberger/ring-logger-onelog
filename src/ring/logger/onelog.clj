(ns ring.logger.onelog
  (:require [clansi.core :as ansi]
            [clj-logging-config.log4j :as log-config]
            [onelog.core :as log]
            [ring.logger :as logger]
            [ring.logger.protocols :refer [Logger]]))

;; TODO: Alter this subsystem to contain a predefined map of all
;; acceptable fg/bg combinations, since some (e.g. white on yellow)
;; are practically illegible.
(def id-colorizations
  "Foreground / background color codes allowable for random ID colorization."
  {:white :bg-white :black :bg-black :red :bg-red :green :bg-green :blue :bg-blue :yellow :bg-yellow :magenta :bg-magenta :cyan :bg-cyan} )

(def id-foreground-colors (keys id-colorizations))
(def id-colorization-count (count id-foreground-colors))

(defn- get-colorization
  [id]
  "Returns a consistent colorization for the given id; that is, the
  same ID produces the same color pattern. The colorization will have
  distinct foreground and background colors."
  (let [foreground (nth id-foreground-colors (mod id id-colorization-count))
        background-possibilities (vals (dissoc id-colorizations foreground))
        background (nth background-possibilities (mod id (- id-colorization-count 1)))]
    [foreground background]))

(defn- format-id [id]
  "Returns a standard colorized, printable representation of a request id."
  (if id
    (apply ansi/style (format "%04x" id) [:bright] (get-colorization id))))

(defrecord OnelogLogger []
  Logger

  (add-extra-middleware [_ handler]
    (fn [request]
      (println "adding logging context")
      (log-config/with-logging-context (format-id (rand-int 0xffff))
        (handler request))))

  (error [_ x] (log/error x))
  (error-with-ex [_ ex x] (log/error (log/throwable ex) x))
  (info [_ x] (log/info x)) 
  (warn [_ x] (log/warn x)) 
  (debug [_ x] (log/debug x))
  (trace [_ x] (log/trace x)))

(defn make-onelog-logger []
  (OnelogLogger.))


(defn wrap-with-logger
  "Returns a Ring middleware handler which uses OneLog as logger.

  Supported options are the same as of ring.logger/wrap-with-logger, except of
  :logger-impl which is fixed to a OnelogLogger instance"
  ([handler options]
   (logger/wrap-with-logger
     handler
     (merge options {:logger-impl (make-onelog-logger)})))
  ([handler] (wrap-with-logger handler {})))
