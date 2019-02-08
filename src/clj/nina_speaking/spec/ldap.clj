(ns nina-speaking.spec.ldap
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as s.gen]))

(def present? (comp not empty?))
(defn hostname? [s]
  (re-matches #"[a-zA-Z\d.-]+\.[a-zA-Z]{2,5}" s))
(defn email? [s]
  (let [[mbox host] (clojure.string/split s #"@" 2)]
    (and (present? mbox)
         (present? host)
         (hostname? host))))

(defn sized-string [n]
  (s.gen/fmap #(apply str %)
              (s.gen/vector (s.gen/char-alpha) n)))
(def tld
  (s.gen/one-of [(sized-string 2) (sized-string 3)]))
(def domain
  (s.gen/fmap (fn [[host tld]] (str host "." tld))
              (s.gen/tuple
               (s.gen/not-empty (s.gen/string-alphanumeric))
               tld)))

(def email-gen
  (s.gen/fmap (fn [[mbox domain]] (str mbox "@" domain))
              (s.gen/tuple
               (s.gen/not-empty (s.gen/string-alphanumeric))
               domain)))

(s/def ::name (s/with-gen (s/and string? present?)
                #(s.gen/fmap (fn [v] (apply str v))
                             (s.gen/vector (s.gen/char-alpha)))))

(s/def ::description (s/and string? present?))
(s/def ::email (s/with-gen (s/and string? email?)
                 (fn [] email-gen)))
(s/def ::mail ::email)
(s/def ::o ::name)
(s/def ::cn ::name)
(s/def ::sn ::name)
(s/def ::dc (s/with-gen ::name
              #(s.gen/elements #{"producers" "consumers" "people"})))
(s/def ::ou (s/with-gen (s/or :many (s/coll-of ::name :min-count 1)
                              :one string?)
              #(s.gen/elements #{["people"]})))

(s/def ::userPassword string?)

(s/def ::objectClass
  (s/with-gen #{#{"organizationalPerson" "inetOrgPerson" "top"}
                #{"organizationalPerson" "inetOrgPerson" "top" "dcObject"}
                #{"top" "organization" "dcObject"}
                #{"top" "organizationalUnit"}}
    #(s.gen/elements [#{"organizationalPerson" "inetOrgPerson" "top"}])))

(s/def ::record (s/keys :req-un [::objectClass]
                        :opt-un [::cn ::mail ::sn ::dc ::o ::ou
                                 ::userPassword ::password]))
(s/def ::person-record (s/keys :req-un [::objectClass ::cn ::mail ::sn ::ou]
                               :opt-un [::userPassword ::description]))

(s/def ::dn #(re-matches #"(cn=[^,]+,)?(ou=[^,]+,)*(dc=\w+,?)+$" %))

(comment
  (s.gen/sample (sized-string 3))
  (s.gen/sample tld)
  (s.gen/sample email-gen)
  (s.gen/sample (s/gen ::email))
  (s.gen/sample (s/gen ::objectClass))
  (s.gen/sample (s/gen ::ou))
  (s.gen/sample (s/gen ::record))


  (s/valid? ::dn "cn=jdoe,ou=producers,ou=people,dc=thetripps,dc=org")
  (s/valid? ::dn "ou=producers,ou=people,dc=thetripps,dc=org")
  (s/valid? ::dn "cn=,ou=producers,ou=people,dc=thetripps,dc=org")

  )
