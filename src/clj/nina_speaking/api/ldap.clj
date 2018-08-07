(ns nina-speaking.api.ldap
  (:require [nina-speaking.data.ldap    :as ldap]
            [compojure.api.sweet        :as api]
            [org.httpkit.server         :as http]
            [com.stuartsierra.component :as component]
            [ring.util.http-response    :as response]))

(defn app-routes
  "Return the web handler function as a closure over the application
  component.

  See: github.com/stuartsierra/component"
  [{:keys [storage] :as app}]
  (api/api
   (api/GET "/index" [] (response/ok {:documents (ldap/all-people storage)}))
   ))

(defrecord WebServer [port http-server app]
  component/Lifecycle

  (start [{:keys [port app] :as this}]
    (if http-server
      this
      (assoc this :http-server
             (http/run-server (app-routes app) {:port port}))))

  (stop  [{:keys [http-server] :as this}]
    (if (nil? http-server)
      this
      (do
        (http-server :timeout 100)
        (assoc this :http-server nil)))))

(defn web-server
  "Return a new instance of the web server component that creates its
  own handler at call-time."
  [port]
  (map->WebServer {:port port}))
