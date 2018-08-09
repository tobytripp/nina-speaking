(defproject nina-speaking "0.1.0-SNAPSHOT"
  :description "An experiment in LDAP-backed SSO using JWT"
  :url "https://github.com/tobytripp/nina-speaking"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure           "1.9.0"]
                 [environ                       "1.1.0"]
                 [com.stuartsierra/component    "0.3.2"]

                 [com.taoensso/timbre           "4.10.0"]

                 [metosin/compojure-api         "2.0.0-alpha21"]
                 [http-kit                      "2.2.0"]
                 [buddy/buddy-auth              "2.1.0"]
                 [hiccup                        "1.0.5"]

                 [org.clojars.pntblnk/clj-ldap  "0.0.16"]]
  :ring {:handler nina-speaking.api.ldap/app}

  :main ^:skip-aot nina-speaking.core

  :target-path    "target/%s"
  :source-paths   ["src", "src/clj"]
  :test-paths     ["test", "test/clj", "src/clj/test"]
  :resource-paths ["resources"]

  :plugins [[lein-pprint "1.1.1"]]

  :profiles {:dev     {:plugins      [[lein-ring "0.12.4"]]
                       :dependencies [[ring/ring-mock "0.3.2"]]}
             :repl    {:plugins [[cider/cider-nrepl "0.17.0"]]}
             :uberjar {:aot :all}}
  :global-vars {*warn-on-reflection* true
                *assert*             true}
  :jvm-opts ["-Xmx1g"])
