(ns nina-speaking.logging
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]))

(def config
  "Timbre Logging Configuration"
  {:level          :debug
   :middleware     []
   :timestamp-opts timbre/default-timestamp-opts
   :output-fn      timbre/default-output-fn

   :appenders
   {:spit (appenders/spit-appender {:fname "log/nina.log"})}})

(timbre/merge-config! config)
