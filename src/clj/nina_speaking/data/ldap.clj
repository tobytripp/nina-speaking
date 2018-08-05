(ns nina-speaking.data.ldap
  (:require [clj-ldap.client :as ldap]))

(def root-dn "dc=thetripps,dc=org")

(def ldap-server
  (ldap/connect {:host "ldap"
                 :bind-dn "cn=admin,dc=thetripps,dc=org"
                 :password "omelet-sever-exposure-averse"}))


(ldap/get ldap-server "cn=admin,dc=thetripps,dc=org")

(ldap/search ldap-server (str "ou=people," root-dn))



