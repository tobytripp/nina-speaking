(ns nina-speaking.data.ldap-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [nina-speaking.data.ldap :refer :all]))

(deftest creating-ldap-records
  (let [store     (component/start (new-storage "ldap"
                                                "cn=admin,dc=thetripps,dc=org"
                                                "omelet-sever-exposure-averse"))
        people    {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that may, incidentally, use this software"}
        providers {:objectClass #{"top" "organizationalUnit"}
                   :ou          "people"
                   :description "People that are health-care providers"}
        joe       {:objectClass #{"organizationalPerson" "inetOrgPerson"
                                  "dcObject" "top"}
                   :cn          "jdoe"
                   :dc          "people"
                   :sn          "Doe"
                   :mail        "john.doe@provider.com"}]
    (try
      (add-record store "dc=thetripps,dc=org"
                  {:objectClass #{"top" "organization" "dcObject"}
                   :o           "Nina"})
      (add-record store "ou=people,dc=thetripps,dc=org"
                  people)

      (add-records store
                   {"ou=providers,ou=people,dc=thetripps,dc=org"         providers
                    "cn=jdoe,ou=providers,ou=people,dc=thetripps,dc=org" joe})

      (is (= ["jdoe"] (filter identity (map :cn (all-people store)))))
      (finally (component/stop store)))))

(comment
  (run-tests 'nina-speaking.data.ldap-test)
  )
