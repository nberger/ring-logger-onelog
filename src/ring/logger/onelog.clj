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

(defmulti format-id
  "Returns a printable representation of a request id.

  Actual output depends on :printer option. By default it's ANSI-colorized.
  To get a non-colorized format use :printer :no-color"
  (fn [{:keys [printer]} & _] printer))

(defmethod format-id :default
  [_ id]
  (apply ansi/style (format "%04x" id) [:bright] (get-colorization id)))

(defmethod format-id :no-color
  [_ id]
  (format "%04x" id))

(defrecord OnelogLogger [printer]
  Logger

  (add-extra-middleware [this handler]
    (fn [request]
      (log-config/with-logging-context (format-id this (rand-int 0xffff))
        (handler request))))

  (log [_ level throwable message]
    (let [throwable (when throwable (log/throwable throwable))]
    (case level
      :error (log/error throwable message)
      :info (log/info throwable message)
      :warn (log/warn throwable message)
      :debug (log/debug throwable message)
      :trace (log/trace throwable message)))))

(defn make-onelog-logger
  ([] (make-onelog-logger {}))
  ([options]
   (map->OnelogLogger options)))

(defn wrap-with-logger
  "Returns a Ring middleware handler which uses OneLog as logger.

  Supported options are the same as of ring.logger/wrap-with-logger, except for
  :logger which is fixed to a OnelogLogger instance"
  ([handler options]
   (println "onelog wrap-with-logger")
   (logger/wrap-with-logger
     handler
     (merge options {:logger (make-onelog-logger options)})))
  ([handler] (wrap-with-logger handler {})))
