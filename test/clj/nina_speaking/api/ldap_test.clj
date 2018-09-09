(ns nina-speaking.api.ldap-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component         :as component]
            [cheshire.core                      :as json]
            [ring.mock.request                  :as mock]

            [cheshire.core :refer [parse-stream]]

            [nina-speaking.test-support.storage :refer :all]
            [nina-speaking.data.ldap            :as storage]
            [nina-speaking.api.ldap :refer :all :as subject]))

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
                {:cn    "jdoe"
                 :dc    "people"
                 :dn    "cn=jdoe,ou=providers,ou=people,dc=thetripps,dc=org"
                 :sn    "Doe"
                 :mail  "john.doe@provider.com"}}))))))



(deftest creating-ldap-records-via-POST
  (with-storage
    (fn [store]
      (let [handler  (api-routes {:storage store})
            response (handler
                      (-> (mock/request :post "/credentials/")
                         (mock/body
                          {"credentials[email]"    "toby@tripp.net"
                           "credentials[role]"     "mediator"
                           "credentials[password]" "angry-hippo-marble-run"})))]
        (is (= 201 (:status response)))
        (is (= {"Location"     "/credential/toby%40tripp.net"
                "Content-Type" "application/json; charset=utf-8"}
               (:headers response)))
        (is (= {:document
                {:dc   "ou=people"
                 :mail "toby@tripp.net"
                 :dn   "cn=toby,ou=mediator,ou=people,dc=thetripps,dc=org"
                 :sn   "Unknown"
                 :cn   "toby"}}
               (parse-stream (clojure.java.io/reader (:body response))
                             true)))))))

