(ns app.views.home
  (:require
    [reagent.core :as r]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [app.repl.core :as repl]
    [markdown.core :refer [md->html]]
    [app.session :as session]
    [app.tutorial :refer [tutorial]]))

(def intro-title
  "Got 5 minutes?")

(def intro-content
  "Tempo is a zero-dependency Clojure(Script) API to java.time on the JVM and <a href=\"https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Temporal\">Temporal</a> on JS runtimes (like this browser)

 <span id=\"location-of-editor\">Here on the right</span>
you have a **REPL**, the same as <a href=\"https://tryclojure.org\">Try Clojure</a> except that
functions from <a href=\"https://github.com/henryw374/tempo\">Tempo</a> are included under the alias 't'

Try to type some expressions as `(t/date-parse \"2020-02-02\")` or click
on code to auto insert. You can type `(help)` for more commands.
   
Type `(start)` when you're ready!")

(defn compute-step
  "Returns a list of `title` and `content`
  based on the current step stored into the session."
  [{:keys [step]}]
  (let [step-data (or (some (fn [{:keys [title] :as step-data}]
                              (and (= title step) step-data)) tutorial)
                    (nth tutorial 0))]
    
    [(:title step-data) (:content step-data)]))

(defn- link-target
  "Add target=_blank to link in markdown."
  [text state]
  [(string/replace text #"<a " "<a target=\"_blank\" ")
   state])

;; Regex to find [[var]] pattern in strings
(def re-doublebrackets #"(\[\[(.+)\]\])")

(defn- session-vars
  "Replace `[[var]]` in markdown text using session
  variables."
  [text state]
  [(let [res (re-find re-doublebrackets text)]
     (if res
       (let [k (keyword (last res))
             v (if (session/has? k)
                 (session/get k)
                 "unset")]
         (string/replace text re-doublebrackets v))
       text))
   state])

(defn- parse-md [s]
  (md->html s :custom-transformers [link-target session-vars]))

;; -------------------------
;; Views
;; -------------------------

(defn- handle-tutorial-click
  "When user click of `code` DOM node, fill the REPL input
  with the code content and focus on it."
  [e]
  (let [target (.-target e)
        node-name (.-nodeName target)]
    (when (= node-name "CODE")
      (->> (.-textContent target)
           (reset! repl/repl-input))
      (repl/focus-input))))

(defn tutorial-view [[title content]]
  [:div {:class    ["bg-gray-200"
                    "text-black"
                    "dark:text-white"
                    "dark:bg-gray-800"
                    "shadow-lg"
                    "sm:rounded-l-md"
                    "xs:rounded-t-md"
                    "w-full"
                    "md:p-8"
                    "p-6"
                    "min-h-[200px]"
                    "opacity-95"]
         :on-click handle-tutorial-click}
   [:h1 {:class ["text-3xl" "mb-4" "font-bold" "tracking-tight"]}
    title]
   [:div {:class                   ["leading-relaxed" "last:pb-0"]
          :dangerouslySetInnerHTML #js{:__html (parse-md content)}}]])

(defn content-view [_params]
  (r/create-class
    {:display-name "home-view"

     :component-did-mount
     (fn []
       ;; Focus on input after first rendered
       (repl/focus-input))

     :reagent-render
     (fn [params]
       [:div {:class ["flex"
                      "sm:flex-row"
                      "flex-col"
                      "items-center"
                      "justify-center"
                      "xl:-mt-32"
                      "lg:-mt-8"
                      "mt-0"]}
        [:div {:class ["flex-1" "z-0"]}
         [tutorial-view (compute-step params)]]
        [:div {:class ["flex-1"
                       "z-10"
                       "sm:w-auto"
                       "w-full"
                       "sm:mt-0"
                       "mt-7"
                       "sm:mb-0"
                       "mb-14"]}
         [repl/view]]])}))

(defn- update-location-of-editor []
  (let [window-width (. js/window -innerWidth)
        location-of-editor-dom (.getElementById js/document "location-of-editor")]
    (when location-of-editor-dom
      (set! (. location-of-editor-dom -innerHTML)
        (if (< window-width 640) "Down below" "Here on the right")))))

(.addEventListener js/window "load" update-location-of-editor)
(.addEventListener js/window "resize" update-location-of-editor)


(defn view [params]
  (r/with-let [menu-open? (r/atom true)]
    [:div#wrapper.d-flex (when-not @menu-open? {:class "sb-sidenav-toggled"})
     [:div#sidebar-wrapper.border-end.bg-white
      [:div.sidebar-heading.border-bottom.bg-light
       [:span "Tempo docs"
        ;[:button.navbar-toggler {:type "button" :data-bs-toggle "collapse" :data-bs-target "#navbarSupportedContent" :aria-controls "navbarSupportedContent" :aria-expanded "false" :aria-label "Toggle navigation"} [:span.navbar-toggler-icon]]
        ]]

      [:div.list-group.list-group-flush
       (->> tutorial
            (map (fn [{:keys [title]}]
                   ^{:key title}
                   [:a.list-group-item.list-group-item-action.list-group-item-light.p-3 
                    {:href (rfe/href :index {} {:step title} )} title])))
       ]]
     [:div#page-content-wrapper
      [:nav.navbar.navbar-expand-lg.navbar-light.bg-light.border-bottom
       [:div.container-fluid
        [:button#sidebarToggle.btn.btn-primary
         {:on-click #(swap! menu-open? not)}
         (if @menu-open? "<<" ">>")]]]
      [:div.container-fluid
       [content-view params]
       ]]])
  )
