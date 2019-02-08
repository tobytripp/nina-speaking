(ns nina-speaking.data.storage.ldap-test
  (:require [clj-ldap.client :as ldap]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [com.stuartsierra.component :as component]
            [nina-speaking.data.storage.ldap :refer :all]
            [nina-speaking.spec.ldap :as spec]
            [nina-speaking.test-support.storage :refer :all]
            [taoensso.timbre :as log]))

(stest/instrument `add-record)

(def ^:dynamic *test-store* (atom nil))

(defn wrap-storage [f]
  (with-storage
    (fn [store]
      (swap! *test-store*
             (fn [old-store]
               (if old-store old-store store)))
      (f)
      (swap! *test-store* (fn [_] nil)))))

(use-fixtures :once wrap-storage)

(deftest cleaning-up-after-ldap-tests
  (let [host                           "ldap"
        dn                             "cn=admin,dc=thetripps,dc=org"
        password                       "omelet-sever-exposure-averse"
        {:keys [connection] :as store} (component/start (new-storage host dn password))
        base-dn                        "ou=people,dc=thetripps,dc=org"]
    (try
      (init-store store)

      (letfn [(children [dn]
                (log/spy (search store "(objectclass=*)" dn
                                 {:attributes [:cn] :scope :subordinate})))
              (children? [dn]
                (< 0 (log/spy (count (children dn)))))]

        (is (log/spy (children? base-dn)))

        (doall
         (for [{:keys [dn]} (children base-dn)
               :when        (not (children? dn))]
           (do
             (is (not (children? dn)))
             (log/debugf "DELETE (%s): %s" base-dn dn)
             (ldap/delete connection dn)))))

      (is (= [] (filter identity (map :cn (all-people store)))))

      (finally (component/stop store)))))

(deftest creating-ldap-records
  (let [joe  {:objectClass #{"organizationalPerson" "inetOrgPerson" "top"}
              :cn          "jdoe"
              :sn          "Doe"
              :ou          "people"
              :mail        "john.doe@producer.com"}
        jane {:objectClass #{"organizationalPerson" "inetOrgPerson" "top"}
              :cn          "jsmith"
              :sn          "Smith"
              :ou          "people"
              :mail        "jane.smith@consumer.com"}

        rando {:objectClass #{"inetOrgPerson" "organizationalPerson" "top"},
               :cn          "A",
               :ou          "people"
               :mail        "j0@8.VGT",
               :sn          "M"}]
    (is (s/valid? ::spec/person-record joe)
        (s/explain-str ::spec/person-record joe))
    (is (s/valid? ::spec/person-record jane)
        (s/explain-str ::spec/person-record jane))
    (is (s/valid? ::spec/person-record jane)
        (s/explain-str ::spec/person-record rando))

    (add-records @*test-store*
                 {"cn=jdoe,ou=people,dc=thetripps,dc=org"   joe
                  "cn=jsmith,ou=people,dc=thetripps,dc=org" jane
                  "cn=A,ou=people,dc=thetripps,dc=org"      rando})

    (is (= "cn=jdoe,ou=people,dc=thetripps,dc=org"
           (dn-for joe "dc=thetripps,dc=org")))
    (is (= ["A" "jdoe" "jsmith"]
           (filter identity (map :cn (all-people @*test-store*)))))
    ))

(defspec creating-arbitrary-ldap-records
  40
  (prop/for-all
   [rec (s/gen ::spec/person-record)]
   (let [dn             (dn-for rec "dc=thetripps,dc=org")
         cn             (:cn rec)
         {:keys [code]} (add-record @*test-store* dn rec)]
     (is (= 0 code))
     (is (contains?
          (->> (all-people @*test-store*)
             (map :cn)
             (filter identity)
             (map clojure.string/lower-case)
             set)
          (clojure.string/lower-case cn))))))

(deftest adding-a-new-role
  (with-storage
    (fn [store]
      (add-role store "cheeky-monkey")
      (is (= [{:ou "cheeky-monkey"
               :dn "ou=cheeky-monkey,ou=people,dc=thetripps,dc=org"}]
             (search store "ou=cheeky-monkey"))))))


(defspec upserting-new-roles
  25
  (prop/for-all [role (gen/not-empty gen/string-alphanumeric)]
                (with-storage
                  (fn [store]
                    (add-role store role)
                    (let [rdn (str "ou=" role)]
                      (= [{:ou role
                           :dn (format"ou=%s,ou=people,dc=thetripps,dc=org" role)}]
                         (search store rdn)))))))

(deftest adding-a-person-given-attributes
  (with-storage
    (fn [store]
      (add-person store
                  {:email    "toby@tripp.test"
                   :role     "code-monkey"
                   :password "angry-code-monkeys"})
      (is (= {:cn   "toby"
              :sn   "Unknown"
              :dn   "cn=toby,ou=code-monkey,ou=people,dc=thetripps,dc=org"
              :mail "toby@tripp.test"}
             (by-email store "toby@tripp.test")))

      (is (= (add-person store
                         {:email    "thomas@tripp.test"
                          :role     "code-monkey"
                          :password "angry-code-monkeys"})
             {:cn   "thomas"
              :sn   "Unknown"
              :dn   "cn=thomas,ou=code-monkey,ou=people,dc=thetripps,dc=org"
              :mail "thomas@tripp.test"}))
      )))

(deftest finding-people-by-email
  (with-storage
    (fn [store]
      (let [joe {:objectClass #{"organizationalPerson" "inetOrgPerson"
                                "dcObject" "top"}
                 :cn          "jcdoe"
                 :dc          "people"
                 :sn          "Doe"
                 :mail        "john.c.doe@provider.com"}]
        (add-records store
                     {"cn=jcdoe,ou=people,dc=thetripps,dc=org" joe})

        (is (= {:cn   "jcdoe"
                :dc   "people"
                :dn   "cn=jcdoe,ou=people,dc=thetripps,dc=org"
                :sn   "Doe"
                :mail "john.c.doe@provider.com"}
               (by-email store "john.c.doe@provider.com")))))))

(deftest password-hashing
  (with-storage
    (fn [store]
      (add-person store
                  {:email    "toby@tripp.test"
                   :role     "code-monkey"
                   :password "angry-monkeys-code"})

      (is (not= "angry-monkeys-code"
                (get-in
                 (first
                  (search store
                          (str "mail=toby@tripp.test")
                          "ou=people,dc=thetripps,dc=org"
                          {:attributes #{:cn :userPassword}}))
                 [:userPassword]))))))



(comment
  (run-tests 'nina-speaking.data.storage.ldap-test)

  (gen/sample (gen/not-empty gen/string-alphanumeric))

  (with-storage
    (fn [store]
      (search store
              "(objectclass=*)"
              "ou=code-monkey,ou=people,dc=thetripps,dc=org"
              {:attributes [:cn]
               :scope      :subordinate})))

  (< 1 2)

  (sgen/generate (s/gen ::spec/email))
  )
