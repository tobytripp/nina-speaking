(ns nina-speaking.test-support.storage
  (:require [com.stuartsierra.component :as component]
            [nina-speaking.data.ldap    :as storage]))

(defn init-store [store]
  (let [org       {:objectClass #{"top" "organization" "dcObject"}
                   :o           "Nina"}
        people    {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that may, incidentally, use this software"}
        providers {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that are health-care providers"}
        suppliers {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that are equipment suppliers"}
        joe       {:objectClass #{"organizationalPerson" "inetOrgPerson"
                                  "dcObject" "top"}
                   :cn          "jdoe"
                   :dc          "people"
                   :sn          "Doe"
                   :mail        "john.doe@provider.com"}
        jane      {:objectClass #{"organizationalPerson" "inetOrgPerson"
                                  "dcObject" "top"}
                   :cn          "jsmith"
                   :dc          "people"
                   :sn          "Smith"
                   :mail        "jane.smith@supplier.com"}]
    (storage/add-records
     store
     {"dc=thetripps,dc=org"                                  org
      "ou=people,dc=thetripps,dc=org"                        people
      "ou=providers,ou=people,dc=thetripps,dc=org"           providers
      "ou=suppliers,ou=people,dc=thetripps,dc=org"           suppliers
      "cn=jdoe,ou=providers,ou=people,dc=thetripps,dc=org"   joe
      "cn=jsmith,ou=suppliers,ou=people,dc=thetripps,dc=org" jane})
    store))

(defn with-storage
  "Execute f with a connected storage instance.  f should take the storage
  instance as a parameter."
  [f]
  (let [host     "ldap"
        dn       "cn=admin,dc=thetripps,dc=org"
        password "omelet-sever-exposure-averse"
        store    (component/start (storage/new-storage host dn password))]
    (try
      (init-store store)
      (f store)
      (finally
        (storage/delete-all! store "ou=people,dc=thetripps,dc=org")
        (component/stop store)))))
