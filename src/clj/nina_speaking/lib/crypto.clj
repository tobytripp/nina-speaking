(ns nina-speaking.lib.crypto
  (:require [buddy.hashers :as hashers]))

(defn hash [password]
  (hashers/derive password {:alg :bcrypt+sha512}))

(defn verify [password hashed]
  (hashers/check password hashed))
