(ns editorclj.index
  (:require-macros
   [javelin.core :refer [defc defc=]])
  (:require
   [clojure.pprint :refer [pprint]]
   [javelin.core :refer [cell]]
   [castra.core :refer [mkremote]]
   [hoplon.core :refer [for-tpl cond-tpl link script nav div button span text a strong ul li]]
   [editorclj.html :as html]))


;; TODO: Find a way to make these pluggable
(defc content-handlers {"text/x-gfm"           {:name "Markdown"
                                                :new identity
                                                :open identity
                                                :rename identity
                                                :save identity}
                        "application/edn"      {:name "Clojure EDN"
                                                :new identity
                                                :open identity
                                                :rename identity
                                                :save identity}
                        "text/x-clojure"       {:name "Clojure"
                                                :new identity
                                                :open identity
                                                :rename identity
                                                :save identity}
                        "text/x-clojurescript" {:name "ClojureScript"
                                                :new identity
                                                :open identity
                                                :rename identity
                                                :save identity}
                        "text/url"             {:name "Embedded IFrame"
                                                :new identity
                                                :open identity
                                                :rename identity
                                                :save identity}})


(defc workspaces [{:name "Home"
                   :system true
                   :contents [{:name "welcome.dm"
                               :id "welcome.dm"        ; For clj/cljs, this is the full package/name; for files, the path/name
                               :local-path "welcome.dm"
                               :remote-path nil
                               :mime-type "text/x-gfm"}
                              {:name "editor-settings.dm"
                               :id "editor-settings.dm" ; For clj/cljs, this is the full package/name; for files, the path/name
                               :local-path "editor-settings.dm"
                               :remote-path nil
                               :mime-type "text/x-gfm"}
                              {:name "workspaces.dm"
                               :id "workspaces.dm" ; For clj/cljs, this is the full package/name; for files, the path/name
                               :local-path "workspaces.dm"
                               :remote-path nil
                               :mime-type "text/x-gfm"}
                              {:name "debug.edn"
                               :id "debug.edn" ; For clj/cljs, this is the full package/name; for files, the path/name
                               :local-path "debug.edn"
                               :remote-path nil
                               :mime-type "application/edn"}]
                   :split {:none "welcome.dm"} ; #{:none buffer, :horizontal {:top buffer|:split {} :bottom buffer}, :vertical...}
                   }])


(defc= sorted-workspaces (apply conj [(first workspaces)] (sort (fn [a b] (< (:name a) (:name b))) (rest workspaces))))
(defc= current-workspace (get workspaces 0))
(defc= buffers-by-mime (let [workspace-contents (:contents current-workspace)]
                           (->> workspace-contents
                                (map :mime-type)
                                (into #{})
                                (vec)
                                (map (fn [mime-type]
                                       [(:name (get content-handlers mime-type mime-type))
                                        (filter #(= mime-type (:mime-type %)) workspace-contents)]))
                                (into {}))))
(defc= buffers-by-key (->> (:contents current-workspace)
                           (map (fn [buffer]
                                  [(:id buffer) buffer]))
                           (into {})))
(defc= buffer-type-names (sort (keys buffers-by-mime)))


(defc= page-title (:name current-workspace))
(defc= page-branding (-> current-workspace :split :none))

(defc log-cell [])
(defc= printer (with-out-str (pprint log-cell)))


(defn navbar []
  (nav :class "navbar navbar-inverse navbar-fixed-top"
            (div :class "container-fluid"
                 (div :class "navbar-header"
                      (button :type "button"
                              :class "navbar-toggle collapsed"
                              :data-toggle "collapse"
                              :data-target "#navbar"
                              :aria-expanded "false"
                              :aria-controls "navbar"

                              (span :class "sr-only"
                                    (text "Toggle navigation"))
                              (span :class "icon-bar")
                              (span :class "icon-bar")
                              (span :class "icon-bar"))
                      (a :class "navbar-brand" :href #(constantly true) (text "~{page-branding}")))
                 (div :id "navbar" :class "navbar-collapse collapse"
                      (ul :class "nav navbar-nav"
                          (li (a :class "workspace-left" :href "#/"
                                 (text "\u21D0")))
                          (li :class "dropdown"
                              (a :href #(constantly true)
                                 :class "dropdown-toggle"
                                 :data-toggle "dropdown"
                                 :role "button"
                                 :aria-haspopup "true"
                                 :aria-expanded "false"
                                 (text "Workspace ")
                                 (span :class "caret"))
                              (ul :class "dropdown-menu"
                                  (li (a :href "#/workspaces/home-workspace" (text "Split horizontally")))
                                  (li (a :href "#/workspaces/home-workspace" (text "Split vertically")))
                                  (li (a :href "#/workspaces/home-workspace" (text "Delete current split")))
                                  (li :role "separator" :class "divider")
                                  (li (a :href "#/workspaces/home-workspace" (text "New buffer...")))
                                  (li (a :href "#/workspaces/home-workspace" (text "Rename buffer...")))
                                  (li (a :href "#/workspaces/home-workspace" (text "Delete current buffer...")))
                                  (li :role "separator" :class "divider")
                                  (for-tpl [w sorted-workspaces]
                                           (li (a :href
                                                  (str "#/workspace/" (:name @w))
                                                  (do (swap! log-cell #(conj % (:name @w)))
                                                      (swap! log-cell #(conj % (:name @current-workspace)))
                                                      (text (:name w))))))))

                          (for-tpl [buffer-type-name buffer-type-names]
                                   (li :class "dropdown"
                                       (a :href "#"
                                          :class "dropdown-toggle"
                                          :data-toggle "dropdown"
                                          :role "button"
                                          :aria-haspopup "true"
                                          :aria-expanded "false"
                                          (text "~{buffer-type-name} ")
                                          (span :class "caret"))
                                       (ul :class "dropdown-menu"
                                           (let [buffers (get @buffers-by-mime @buffer-type-name)]
                                             (for-tpl [buffer (sort (fn [a b] (< (:id a) (:id b))) buffers)]
                                                      (li (a :href (str "#/workspace/" (:name @current-workspace)  "/buffer/" (:id @buffer)) (text (:name buffer))))))))))
                      (ul :class "nav navbar-nav navbar-right"
                          (li (a :class "workspace-right" :href "#" (text "\u21D2"))))))))


(def css
  [; font-family: 'Source Sans Pro', sans-serif;
   ; font-family: 'Source Code Pro', monospace;
   ; font-family: 'Source Serif Pro', serif;
   (link :href "https://fonts.googleapis.com/css?family=Source+Code+Pro|Source+Sans+Pro|Source+Serif+Pro" :rel "stylesheet")

   ; font-family: 'Josefin Slab', serif;
   ; font-family: 'Josefin Sans', sans-serif;
   (link :href "https://fonts.googleapis.com/css?family=Josefin+Sans|Josefin+Slab" :rel "stylesheet")

   (link :rel "stylesheet"
         :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
         :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
         :crossorigin "anonymous")

   (link :rel "stylesheet"
         :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"
         :integrity "sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r"
         :crossorigin "anonymous")

   (link :rel "stylesheet"
         :href "/ie10/ie10-viewport-bug-workaround.css")

   (link :rel "stylesheet"
         :href "/splitter/splitter.css")

   (link :rel "stylesheet"
         :href "app.css")])


(def scripts
  [(script :src "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
           :integrity "sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS"
           :crossorigin "anonymous")

   (script :src "/ie10/ie10-viewport-bug-workaround.js")

   (script :src "/splitter/splitter.min.js")

   (script :src "http://cdn.jsdelivr.net/pouchdb/6.0.7/pouchdb.min.js")])


()
