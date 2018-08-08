(ns nina-speaking.data.ldap-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [nina-speaking.data.ldap :refer :all]))

(deftest creating-ldap-records
  (let [store (component/start (new-storage "ldap"
                                            "cn=admin,dc=thetripps,dc=org"
                                            "omelet-sever-exposure-averse"))]
    (try
      (is (empty? (all-people store)))
      (add-record store "dc=thetripps,dc=org"
                  {:objectClass #{"top" "organization" "dcObject"}
                   :o "Nina"})
      (add-record store "ou=people,dc=thetripps,dc=org"
                  {:objectClass #{"top" "organizationalUnit"}
                   :description
                   "People that may, incidentally, use this software"})

      (finally (component/stop store)))))

(comment
  (run-tests 'nina-speaking.data.ldap-test)
  )
