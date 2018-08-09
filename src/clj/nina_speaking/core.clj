(ns nina-speaking.core
  (:require [com.stuartsierra.component :as component]
            [nina-speaking.system       :as system])
  (:gen-class))

(def system nil)

(defn init [options]
  (alter-var-root #'system
                  (constantly (system/new-system options))))

(defn start []
  (alter-var-root #'system component/start))
(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn -main
  "Start the system and serve API requests"
  [& args]
  (component/start (system/new-system {})))

(comment
  (def dev-systems "Last system Component" (atom ()))
  (swap! dev-systems
         conj
         (init
          {:ldap-host "ldap"
           :dn         "cn=admin,dc=thetripps,dc=org"
           :password   "omelet-sever-exposure-averse"
           :port       80}))

  (start)
  (stop)

  (do (stop)
      (start))

  nina-speaking.core/system
  )
