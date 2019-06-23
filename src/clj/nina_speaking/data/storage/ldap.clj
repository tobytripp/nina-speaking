(ns nina-speaking.data.storage.ldap
  (:require [clj-ldap.client            :as ldap]
            [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [nina-speaking.lib.crypto :as crypto]
            [nina-speaking.spec.ldap :as spec]
            [taoensso.timbre :as log]))

(def root-dn "dc=thetripps,dc=org")
(def person-root-dn (str "ou=people," root-dn))
(def ldap-keys #{:sn :dc :mail :userPassword})
(def result-keys #{:ou :cn :sn :dc :mail})

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

(defn search
  ([store]      (search store "(objectclass=*)" (str "ou=people," root-dn)))
  ([store q]    (search store q (str "ou=people," root-dn)))
  ([store q dn] (search store q dn {:attributes result-keys}))
  ([{:keys [connection]} q base-dn options]
   (try
     (ldap/search connection base-dn (merge options {:filter q}))
     (catch com.unboundid.ldap.sdk.LDAPSearchException e
       (log/error e "Search Exception")
       []))))

(defn all-people [store] (search store))

(defn by-email [store email]
  (try
    (first
     (search store (str "mail=" email)))
    (catch com.unboundid.ldap.sdk.LDAPSearchException e
      (log/error "Search Exception" e)
      [])))

(defn dn-for
  "Given an LDIF map `record`, return the fully-qualified DN for the
  `record` in `root-dn`."
  [record root-dn]
  (let [{cn :cn [arity ou] :ou} (s/conform ::spec/record record)
        ous                     (case arity
                                  :one  (set [ou])
                                  :many (set ou))
        elements
        (concat [(str "cn=" cn)]
                (map #(str "ou=" %) (filter (comp not empty?) ous))
                [root-dn])]
    (try
      (clojure.string/join "," elements)
      (catch Exception e
        (log/errorf "Failed to determine DN from %s: %s" record e)
        (throw e)))))


(defn add-record
  "Add a record at the given `DN` with the specified attributes to the
  credential-store.

  Returns `nil` if the record already exists."
  [{:keys [connection]} dn attributes]
  (letfn [(hash-values [m ks]
            (reduce
             (fn [m' k] (update m' k crypto/hash))
             m
             (filter #(% m) ks)))]
    (try
      (log/infof "Insert DN `%s'…" dn)
      (if (ldap/get connection dn #{:cn})
        (do (log/debugf "DN %s 👍" dn)
            (merge {:code 0} attributes))
        (log/spy
         (ldap/add connection dn
                   (hash-values attributes [:userPassword :password]))))
      (catch com.unboundid.ldap.sdk.LDAPSearchException e
        (log/error e "LDAP insert Exception")
        attributes)
      (catch com.unboundid.ldap.sdk.LDAPException e
        (log/warn (format "LDAP insert failed: %s" attributes) e)
        attributes))))
(s/fdef add-record
  :args (s/cat :storage #(instance? CredentialStorage %)
               :dn ::spec/dn
               :attributes ::spec/record)
  :ret (s/nilable ::spec/record))

(defn add-records
  "Add a Map, `recordm` of records to the credential `store`.

  Each Map Entry key is taken to be the record's RDN and the value is the
  record's attributes."
  [store recordm]
  (reduce-kv (fn [memo rdn rec]
               (conj memo rdn (add-record store rdn rec)))
             []
             recordm))

(defn add-role
  "Assert that the given `role-name` is present in the credential store.
  Create it if not."
  [store role-name]
  (add-record store (str "ou=" role-name "," person-root-dn)
              {:objectClass #{"top" "organizationalUnit"}
               :ou          role-name}))


(defn add-person
  "Add a new Person record to the credential store.

  The provided `role` will be created iff it does not yet exist."
  [store {:keys [email role password] :as attrs}]
  (log/infof "add-person: %s, %s, --redacted--" email role)
  (let [[local domain] (clojure.string/split email #"@")
        dn             (str "cn=" local ",ou=" role "," person-root-dn)
        ldif           (merge {:objectClass  #{"organizationalPerson"
                                               "inetOrgPerson"
                                               "top"}
                               :sn           "Unknown"
                               :userPassword password
                               :mail         email}
                              (select-keys attrs ldap-keys))]
    (s/explain ::spec/record ldif)
    (add-role store role)
    (add-record store dn ldif)
    (by-email store email)))

(defn delete-record!
  "Remove a record.

  Warning: this should never be used outside of development/testing."
  [{:keys [connection]} rdn]
  (try
    (ldap/delete connection rdn
                 {:pre-read result-keys})
    (catch com.unboundid.ldap.sdk.LDAPException e
      (log/warn e))))

(def ^:const SUBTREE-DELETE-UNSUPPORTED 66)

(defn recursive-delete!
  "For LDAP servers that don't support the subtree control."
  [{:keys [connection] :as store} base-dn]
  (letfn [(children [dn]
            (search store "(objectclass=*)" dn
                    {:attributes [:cn] :scope :subordinate}))
          (children? [dn]
            (< 0 (count (children dn))))]
    (doall
     (for [{:keys [dn]} (children base-dn)
           :when        (not (children? dn))]
       (do
         (log/debugf "DELETE (%s): %s" base-dn dn)
         (ldap/delete connection dn))))))

(defn delete-all! [{:keys [connection] :as store} dn]
  (try
    (ldap/delete connection dn {:delete-subtree true})
    (catch com.unboundid.ldap.sdk.LDAPException e
      (if (= SUBTREE-DELETE-UNSUPPORTED (.. e getResultCode intValue))
        (recursive-delete! store dn)
        (log/warn e)))))

