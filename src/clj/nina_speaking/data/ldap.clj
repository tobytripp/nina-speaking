(ns nina-speaking.data.ldap
  (:require [clj-ldap.client :as ldap]
            [com.stuartsierra.component :as component]))

(def root-dn "dc=thetripps,dc=org")

(defrecord CredentialStorage [host dn password connection]
  component/Lifecycle

  (start [{:keys [host dn password] :as this}]
    (if connection
      this
      (assoc this :connection
             (ldap/connect {:host     host     ; "ldap"
                            :bind-dn  dn       ; "cn=admin,dc=thetripps,dc=org"
                            :password password ; "omelet-sever-exposure-averse"
                            }))))

  (stop [{:keys [connection] :as this}]
    (if (not connection)
      this
      (do
        (ldap/close connection)
        (assoc this :connection nil)))))

(defn new-storage [host dn password]
  (map->CredentialStorage {:host host :dn dn :password password}))


(defn all-people [{:keys [connection] :as this}]
  (ldap/search connection (str "ou=people," root-dn)))
