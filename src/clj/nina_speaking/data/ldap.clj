(ns nina-speaking.data.ldap
  (:require [clj-ldap.client :as ldap]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]))

(def root-dn "dc=thetripps,dc=org")

(defrecord CredentialStorage [host dn password connection]
  component/Lifecycle

  (start [{:keys [host dn password] :as this}]
    (if connection
      this
      (assoc this :connection
             (ldap/connect {:host     host
                            :bind-dn  dn
                            :password password
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
  (try
    (ldap/search connection (str "ou=people," root-dn))
    (catch com.unboundid.ldap.sdk.LDAPSearchException e
        (log/error "Search Exception" e)
        [])))

(comment
  (let [store (:storage nina-speaking.core/system)]
    (all-people store))
  )
