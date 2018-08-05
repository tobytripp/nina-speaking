(defproject nina-speaking "0.1.0-SNAPSHOT"
  :description "An experiment in LDAP-backed SSO using JWT"
  :url "https://github.com/tobytripp/nina-speaking"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]

                 [metosin/compojure-api "2.0.0-alpha21"]

                 [org.clojars.pntblnk/clj-ldap "0.0.16"]

                 [buddy/buddy-auth "2.1.0"]]

  :main ^:skip-aot nina-speaking.core

  :target-path "target/%s"
  :source-paths ["src", "src/clj"]
  :test-paths ["test", "test/clj", "src/clj/test"]
  :resource-paths ["resources"]

  :plugins [[lein-pprint "1.1.1"]]

  :profiles {:repl {:plugins [[cider/cider-nrepl "0.18.0-SNAPSHOT"]]}
             :uberjar {:aot :all}}
  :global-vars {*warn-on-reflection* true
                *assert* true}
  :jvm-opts ["-Xmx1g"])
