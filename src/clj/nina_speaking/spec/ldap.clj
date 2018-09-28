(ns nina-speaking.spec.ldap
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as s.gen]))

(def present? (comp not empty?))
(defn hostname? [s]
  (re-matches #"[a-zA-Z\d.-]+\.[a-zA-Z]{2,3}" s))
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

(s/def ::name  (s/and string? present?))
(s/def ::email (s/with-gen (s/and string? email?)
                 (fn [] email-gen)))
(s/def ::mail ::email)
(s/def ::cn ::name)
(s/def ::sn ::name)
(s/def ::dc ::name)
(s/def ::objectClass (s/with-gen #(= % #{"organizationalPerson" "inetOrgPerson"
                                         "dcObject" "top"})
                       #(s.gen/elements [#{"organizationalPerson" "inetOrgPerson"
                                           "dcObject" "top"}])))

(s/def ::record (s/keys :req [::objectClass ::cn ::mail]
                        :opt [::sn ::dc]))

(comment
  (s.gen/sample (sized-string 3))
  (s.gen/sample tld)
  (s.gen/sample email-gen)
  (s.gen/sample (s/gen ::email))
  (s.gen/sample (s/gen ::objectClass))
  (s.gen/sample (s/gen ::record))
  )
