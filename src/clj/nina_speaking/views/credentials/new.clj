(ns nina-speaking.views.credentials.new
  (:require [hiccup.core :refer :all]
            [hiccup.form :refer [form-to]]
            [hiccup.page :refer [html5 include-css]]))

(defn layout [& body]
  (html5 {:lang "en"}
         [:head
          [:title "Credentials"]
          (include-css "/styles.css")]
         [:body body]))

(defn new []
  (layout
   [:h1 "New"]
   (form-to {:id "credentials-new"} [:post "/credentials"]
            [:fieldset
             [:legend "Credentials"]
             [:label "E-Mail"
              [:input#credentials-email {:name "credentials[email]"
                                         :type "email"
                                         :placeholder "email"
                                         :required true
                                         :autocomplete "email"}]]
             [:label "Role"
              [:input#credentials-role {:name "credentials[role]"
                                        :type "text"
                                        :list "roles"}]
              [:datalist#roles
               [:option {:value "provider"}]
               [:option {:value "supplier"}]
               [:option {:value "mediator"}]]]

             [:label "Password"
              [:input#credentials-password {:name "credentials[password]"
                                            :required true
                                            :type "password"}]]
             [:input {:type "submit" :value "Create"}]])))
