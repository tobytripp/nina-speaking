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

(defn add-record [{:keys [connection]} rdn attributes]
  "Add a record at the given RDN with the specified attributes to the
  credential-store."
  (try
    (log/debugf "LDAP insert: %s: %s" rdn attributes)
    (ldap/add connection rdn attributes)
    (catch com.unboundid.ldap.sdk.LDAPSearchException e
      (log/error "LDAP insert Exception" e)
      attributes)
    (catch com.unboundid.ldap.sdk.LDAPException e
      (log/warn (format "LDAP insert failed: %s" attributes) e)
      attributes)))

(defn add-records [store recordm]
  (reduce-kv (fn [memo rdn rec]
               (conj memo rdn (log/spy (add-record store rdn rec))))
             []
             recordm))


(comment
  (let [store (:storage nina-speaking.core/system)]
    (all-people store))
  )
