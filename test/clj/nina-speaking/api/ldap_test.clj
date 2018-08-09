(ns nina-speaking.api.ldap-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component         :as component]
            [cheshire.core                      :as json]
            [ring.mock.request                  :as mock]

            [nina-speaking.data.ldap            :as storage]
            [nina-speaking.api.ldap :refer :all :as subject]))

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
        joe {:objectClass #{"organizationalPerson" "inetOrgPerson"
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
      (finally (component/stop store)))))

(defn parse-body [body]
  (json/parse-string (slurp body) true))


(deftest getting-a-record-by-email
  (with-storage
    (fn [store]
      (let [handler (api-routes {:storage store})
            response (handler
                      (mock/request :get "/credential/john.doe%40provider.com"))]
        (is (= (:status response) 200))
        (is (= (parse-body (:body response))
               {:document
                {:cn   "jdoe"
                 :dn "cn=jdoe,ou=providers,ou=people,dc=thetripps,dc=org"
                 :sn   "Doe"
                 :mail "john.doe@provider.com"}}))))))



(deftest creating-ldap-records-via-POST
  (with-storage
    (fn [store]
      (let [handler (api-routes {:storage store})]
        (is (= (handler
                (-> (mock/request :post "/credentials/")
                   (mock/body {"credentials[email]"    "toby@tripp.net"
                               "credentials[role]"     "mediator"
                               "credentials[passwerd]" "angry-hippo-marble-run"})))
               {:status  201
                :headers {"content-type" "application/json"}
                :body    {:key "your expected result"}}))))))

