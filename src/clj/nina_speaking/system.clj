(ns nina-speaking.system
  (:require [com.stuartsierra.component :as component]
            [nina-speaking.data.storage.ldap :as data]
            [nina-speaking.api.ldap  :as api]))

(defrecord AuthSystem [options storage api app]
  component/Lifecycle

  (start [this]
    (component/start-system this [:storage :api :app]))
  (stop [this]
    (component/stop-system this [:storage :api :app])))

(defrecord AppComponent [options storage]
  component/Lifecycle
  (start [this] this)
  (stop [this] this))

(defn new-system [options]
  (let [{:keys [port ldap-host dn password]} options]
    (map->AuthSystem
     {:options options
      :storage (data/new-storage ldap-host dn password)
      :app     (component/using (map->AppComponent options) [:storage])
      :api     (component/using (api/web-server port) [:app])})))


