(ns nina-speaking.api.ldap-test
  (:require [clojure.test :refer :all]
            [clojure.java.io                    :as io]
            [com.stuartsierra.component         :as component]
            [cheshire.core                      :as json]
            [ring.mock.request                  :as mock]

            [cheshire.core :refer [parse-stream]]

            [nina-speaking.test-support.storage :refer :all]
            [nina-speaking.data.storage.ldap            :as storage]
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
        (is (= {:document
                {:cn    "jdoe"
                 :dn    "cn=jdoe,ou=producers,ou=people,dc=thetripps,dc=org"
                 :ou    "producers"
                 :sn    "Doe"
                 :mail  "john.doe@provider.com"}}
               (parse-body (:body response))))))))



(deftest creating-ldap-records-via-POST
  (with-storage
    (fn [store]
      (let [handler  (api-routes {:storage store})
            record   {:mail "toby@tripp.test"
                      :dn   "cn=toby,ou=moderator,ou=people,dc=thetripps,dc=org"
                      :sn   "Unknown"
                      :cn   "toby"}
            response (handler
                      (-> (mock/request :post "/credentials/")
                         (mock/body
                          {"credentials[email]"    "toby@tripp.test"
                           "credentials[role]"     "moderator"
                           "credentials[password]" "angry-hippo-marble-run"})))]
        (testing "POST to /credentials"
          (is (= 201 (:status response)))
          (is (= {"Location"     "/credential/toby%40tripp.test"
                  "Content-Type" "application/json; charset=utf-8"}
                 (:headers response)))
          (is (= {:document record}
                 (parse-stream (io/reader (:body response))
                               true))))
        (testing "Location response"
          (let [follow-request
                (mock/request :get (get-in response [:headers "Location"]))]
            (is (= {:document record}
                   (parse-stream (io/reader (:body (handler follow-request)))
                                 true)))))))))

