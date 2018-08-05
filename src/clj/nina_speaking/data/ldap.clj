(ns nina-speaking.data.ldap
  (:require [clj-ldap.client :as ldap]))

(def ldap-server
  (ldap/connect {:host "ldap"
                 :bind-dn "cn=admin,dc=thetripps,dc=org"
                 :password "omelet-sever-exposure-averse"}))


(ldap/get ldap-server "cn=admin,dc=thetripps,dc=org")

(ldap/add ldap-server "cn=people,ou=groups,dc=thetripps,dc=org"
          {:objectClass #{"top" "posixGroup"}
           :gidNumber 678})

(ldap/add ldap-server "uid=test,ou=people,dc=thetripps,dc=org"
          {:objectClass #{"top" "account"}
           :uid test})
