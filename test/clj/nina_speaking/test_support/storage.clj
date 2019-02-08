(ns nina-speaking.test-support.storage
  (:require [com.stuartsierra.component :as component]
            [nina-speaking.data.storage.ldap    :as storage]
            [taoensso.timbre :as log]
            [clojure.spec.alpha :as s]
            [nina-speaking.spec.ldap :as spec]))

(defn init-store [store]
  (let [org       {:objectClass #{"top" "organization" "dcObject"}
                   :o           "Nina"}
        people    {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that may, incidentally, use this software"}
        producers {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that are health-care producers"}
        consumers {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that are equipment consumers"}
        joe       {:objectClass #{"organizationalPerson" "inetOrgPerson" "top"}
                   :cn          "jdoe"
                   :sn          "Doe"
                   :ou          "producers"
                   :mail        "john.doe@provider.com"}
        jane      {:objectClass #{"organizationalPerson" "inetOrgPerson" "top"}
                   :cn          "jsmith"
                   :sn          "Smith"
                   :ou          "supplers"
                   :mail        "jane.smith@supplier.com"}]
    (s/explain ::spec/record people)
    (s/explain ::spec/record producers)
    (s/explain ::spec/record joe)

    (storage/add-records
     store
     {"dc=thetripps,dc=org"                                  org
      "ou=people,dc=thetripps,dc=org"                        people
      "ou=producers,ou=people,dc=thetripps,dc=org"           producers
      "ou=consumers,ou=people,dc=thetripps,dc=org"           consumers
      "cn=jdoe,ou=producers,ou=people,dc=thetripps,dc=org"   joe
      "cn=jsmith,ou=consumers,ou=people,dc=thetripps,dc=org" jane})
    store))

(defn with-storage
  "Execute `f` with a connected storage instance.  `f` should take the storage
  instance as a parameter."
  [f]
  (let [host     "ldap"
        dn       "cn=admin,dc=thetripps,dc=org"
        password "omelet-sever-exposure-averse"
        store    (component/start (storage/new-storage host dn password))]
    (try
      (log/info "\t----\tstorage setup")
      (init-store store)
      (log/info "\t===>\ttest begins")
      (f store)
      (finally
        (log/info "\t<---\tstorage teardown")
        (storage/delete-all! store "ou=people,dc=thetripps,dc=org")
        (log/info "\t -- \tstorage close")
        (component/stop store)))))

(comment

  (with-storage (fn [store]))
  )
