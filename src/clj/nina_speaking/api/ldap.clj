(ns nina-speaking.api.ldap
  (:require [compojure.api.sweet        :as api :refer [GET POST]]
            [compojure.core             :as core]
            [compojure.route            :as static]
            [org.httpkit.server         :as http]
            [com.stuartsierra.component :as component]
            [ring.util.http-response    :as response]

            [nina-speaking.data.ldap    :as ldap]
            [nina-speaking.views.credentials :as views]))

(defn api-routes
  "Return the web handler function as a closure over the application
  component.

  See: github.com/stuartsierra/component"
  [{:keys [storage] :as app}]
  (api/api
   (api/context "/credentials" []
     (GET "/index" [] (response/ok {:documents (ldap/all-people storage)}))
     (POST "/" request
       ;; params come in “Rails” style: "credentials[email]", need to remember
       ;; how to destructure them here.
       ;; :form-params [email role password]
       (response/ok {:parameters (:form-params request)}))
     )))

(defn app-routes [app]
  (core/routes
   (api-routes app)
   (core/GET "/credentials/new" [] (views/new))
   (static/resources "/")
   (static/not-found "404 Not Found")))


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
