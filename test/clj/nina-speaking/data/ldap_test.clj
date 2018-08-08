(ns nina-speaking.data.ldap-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [nina-speaking.data.ldap :refer :all]))

(deftest creating-ldap-records
  (let [store     (component/start (new-storage "ldap"
                                                "cn=admin,dc=thetripps,dc=org"
                                                "omelet-sever-exposure-averse"))
        org       {:objectClass #{"top" "organization" "dcObject"}
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
    (try
      (add-records store
                   {"dc=thetripps,dc=org"                                  org
                    "ou=people,dc=thetripps,dc=org"                        people
                    "ou=providers,ou=people,dc=thetripps,dc=org"           providers
                    "ou=suppliers,ou=people,dc=thetripps,dc=org"           suppliers
                    "cn=jdoe,ou=providers,ou=people,dc=thetripps,dc=org"   joe
                    "cn=jsmith,ou=suppliers,ou=people,dc=thetripps,dc=org" jane})

      (is (= ["jdoe" "jsmith"] (filter identity (map :cn (all-people store)))))
      (finally (component/stop store)))))

(deftest finding-people-by-email
  (let [store     (component/start (new-storage "ldap"
                                                "cn=admin,dc=thetripps,dc=org"
                                                "omelet-sever-exposure-averse"))
        org       {:objectClass #{"top" "organization" "dcObject"}
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
    (try
      (add-records store
                   {"dc=thetripps,dc=org"                                  org
                    "ou=people,dc=thetripps,dc=org"                        people
                    "ou=suppliers,ou=people,dc=thetripps,dc=org"           suppliers
                    "ou=providers,ou=people,dc=thetripps,dc=org"           providers
                    "cn=jdoe,ou=providers,ou=people,dc=thetripps,dc=org"   joe
                    "cn=jsmith,ou=suppliers,ou=people,dc=thetripps,dc=org" jane})

      (is (= {:cn   "jdoe"
              :dn   "cn=jdoe,ou=providers,ou=people,dc=thetripps,dc=org"
              :sn   "Doe"
              :mail "john.doe@provider.com"}
           (by-email store (:mail joe))))
      (finally (component/stop store)))))

(comment
  (run-tests 'nina-speaking.data.ldap-test)
  )
