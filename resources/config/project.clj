(defproject nina-speaking "0.1.0-SNAPSHOT"
  :description "An experiment in LDAP-backed SSO using JWT"
  :min-lein-version "2.8.3"
  ;; TODO: unify this url somehow (nina-speaking.com?)
  :url "https://github.com/tobytripp/nina-speaking"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure           "1.9.0"]
                 [environ                       "1.1.0"]
                 [com.stuartsierra/component    "0.3.2"]

                 [com.taoensso/timbre           "4.10.0"]

                 [metosin/compojure-api         "2.0.0-alpha28"]
                 [http-kit                      "2.3.0"]
                 [buddy/buddy-auth              "2.1.0"]
                 [buddy/buddy-hashers           "1.3.0"]
                 [hiccup                        "1.0.5"]

                 [org.clojars.pntblnk/clj-ldap  "0.0.16"]]

  :ring {:handler nina-speaking.api.ldap/app}

  :main ^:skip-aot nina-speaking.core

  :target-path    "target/%s"
  :source-paths   ["src", "src/clj"]
  :test-paths     ["test", "test/clj", "src/clj/test"]
  :resource-paths ["resources"]

  :plugins [[lein-pprint "1.1.1"]]

  :profiles {:dev  {:plugins      [[lein-ring "0.12.4"]]
                    :dependencies [[ring/ring-mock "0.3.2"]
                                   [org.clojure/test.check "0.10.0-alpha3"]]
                    :repl-options {:host "0.0.0.0"}}
             :repl {:plugins [[refactor-nrepl "2.4.0"]
                              [cider/cider-nrepl "0.20.0"]]}}
  :uberjar {:aot :all}
  :global-vars {*warn-on-reflection* true
                *assert*             true}
  :jvm-opts ["-Xmx1g"])
