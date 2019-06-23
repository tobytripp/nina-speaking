(ns nina-speaking.lib.crypto-test
  (:require  [clojure.test :as t :refer [deftest is testing]]
             [nina-speaking.lib.crypto :as crypto]))

(deftest password-hashing
  (is (not= "angry-hippo-marble-run"
            (crypto/hash "angry-hippo-marble-run")))
  (let [hashed (crypto/hash "angry-hippo-marble-run")]
    (is (crypto/verify "angry-hippo-marble-run" hashed))))

