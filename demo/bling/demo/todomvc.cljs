(ns bling.demo.todomvc
  (:require
    [bling.core :as bling]
    [cljs.reader :as reader]
    [cljs.core.match :refer-macros [match]]))

(def local-storage-key "todomvc.bling.state")

(def filters
  {:all       (fn [] true)
   :active    (complement :completed)
   :completed :completed})

(defn todomvc-controller [state event]
  (let [find-index-by
        (fn [coll id f]
          (first
            (keep-indexed
              #(if (= id (f %2)) %1) coll)))                ;; TODO

        inc-next-id
        #(update % :next-id inc)]

    ;; (print (str "-----> "  arg " : " (:todos state) "|" (keep-indexed #(if (= arg (:id %2)) %1) (:todos state)) "|")); (find-index-by-id (:todos state) 2))
    (let [new-state
          (match event
                 [:set-filter filter]
                 (assoc state :active-filter filter)

                 [:clear-completed-todos]
                 (update state :todos
                         #(vec (filter (complement :completed) %)))

                 [:set-todo-completed id completed]
                 (assoc-in state
                           [:todos (+ 0 (find-index-by-id (:todos state) arg)) :completed] completed) ;; TODO!!!!

                 [:set-todo-completed-for-all completed]
                 (update state :todos
                         #(mapv
                           (fn [todo]
                             (assoc todo :completed completed)) %))

                 [:add-todo text]
                 (inc-next-id (update state :todos
                                      #(conj % {:id (:next-id state) :text text :completed false})))

                 [:set-todo-text todo-id text]
                 (assoc-in state [:todos todo-id :text] text)

                 [:remove-todo todo-id]
                 (update state :todos
                         #(vec
                           (filter
                             (fn [todo]
                               (not= (:id todo) todo-id)) %))))]

      (.setItem js/localStorage local-storage-key (str new-state))

      new-state)))

;; --- component: todomvc-app -----------------------------------

(defn todomvc-app-view [props state send]
  [todomvc-header :on-new-todo (send! :parent :add-todo value)]
  [todomvc-main :on-remove]
  [todomvc-footer])

(defn- todomvc-initial-state-provider
  []
  ((let [state (.getItem js/localStorage local-storage-key)]
     (if (nil? state)
       {:todos [], :active-filter :all, :next-id 0}
       (reader/read-string state)))))

(def todomvc-app
  (bling/create-component-class
    :type-name "todomvc-main"
    :view todomvc-main-view
    :controller todomvc-controller
    :initial-state {:todos [] :active-filter :all :next-id 0} nil))


(defn todomvc-header-view [props publish state dispatch]
  [:section.todoapp
   [:header.header
    [:h1 "todos"]
    [:input.new-todo
     {:placeholder "What needs to be done?"
      :autofocus   true
      :on-key-down #(if (= (.-keyCode %) 13)
                     (let [value (.-target.value %)]
                       (set! (.-target.value %) "")
                       (send! :parent :on-new-todo value)))}]]])









;; --- component: todomvc-app --------------------------------
(def todomvc-app
  (bling/create-component-class
    {:name
     "todomvc-all"

     :initial-state
     :state-transitions

     {:set-filter
      (fn [state filter]
        (assoc state :active-filter filter))


      :set-filter
      (fn [filter]
        (fn [state]
          (assoc state :active-filter filter)))


      :clear-completed-todos
      (fn [state]
        (assoc state :todos
                     #(vec
                       (filter (complement :completed) %))))

      :set-todo-completed
      (fn [state id completed]
                   (assoc-in state
                             [:todos (+ 0 (find-index-by-id (:todos state) arg)) :completed] completed) ;; TODO!!!!

                   [:set-todo-completed-for-all completed]
                   (update state :todos
                           #(mapv
                             (fn [todo]
                               (assoc todo :completed completed)) %))

                   [:add-todo text]
                   (inc-next-id (update state :todos
                                        #(conj % {:id (:next-id state) :text text :completed false})))

                   [:set-todo-text todo-id text]
                   (assoc-in state [:todos todo-id :text] text)

                   [:remove-todo todo-id]
                   (update state :todos
                           #(vec
                             (filter
                               (fn [todo]
                                 (not= (:id todo) todo-id)) %))))]

        (.setItem js/localStorage local-storage-key (str new-state)))))


;; --- component: todomvc-header --------------------------------
(def todomvc-header
  (bling/create-component-class
    {:name
     "todomvc-header"

     :default-props
     {:on-entered-todo nil}

     :initial-state
     {:text ""}

     :state-transitions
     {:set-text
      (fn [state text] (assoc state :text text))

      :clear-text
      (fn [state] assoc state :text "")}

     :view
     (fn [props publish! state dispatch!]
       [:div
        [:header.header
         [:h1 "todos"]
         [:input.new-todo
          {:placeholder "What needs to be done?"
           :value       (:text state)
           :autofocus   true
           :on-key-down #(if (= (.-keyCode %) 13)
                          (do (publish! :on-entered-todo (:text state))
                              (dispatch :clear-text))
                          (dispatch! :set-text (.-target.value)))
           :on-blur     (do (publish! :on-entered-todo (:text state))
                            (dispatch! :clear-text))}]]])}))


;; --- component: todomvc-items --------------------------------
;; --- component: todomvc-footer --------------------------------
(def todomvc-footer
  (bling/create-component-class
    {:name
     "todomvc-footer"

     :default-props
     {:active-filter    :all
      :on-filter-change nil}

     :view
     (fn [prop publish! _ _]
       [:footer.footer
        [:span.todo-count
         [:strong active-todo-count] " items left"]         ;; TODO (item vs items)
        [:ul.filters>li
         [(if (= active-filter :all) :a.selected :a)
          {:on-click #(publish! :on-filter-change :all)
           :href     "#"}
          "All"]

         [(if (= active-filter :active)
            :a.selected :a)
          {:on-click #(publish! :on-filter-change :active)
           :href     "#"} "Active"]

         [(if (= active-filter :completed)
            :a.selected :a)
          {:on-click #(publish! :on-filter-change :completed)
           :href     "#"} "Completed"]]])}))



(if (< active-todo-count total-todo-count)
  [:button.clear-completed
   {:on-click #(dispatch [:clear-completed-todos])}
   "Clear completed"])

[:footer.info
 [:p " Double-click to edit a todo"]
 [:p " Created by " [:a {:href "http://www.google.com"} "Ralf"]]
 [:p " Part of " [:a {:href "http://todomvc.com"} "TodoMVC"]]] ] ] ) )




;; --- component: todomvc-items --------------------------------

(defn todomvc-items-view [props state dispatch]
  (let [todos (:todos state)
        active-filter (:active-filter state)
        visible-todos (filter (active-filter filters) todos)
        total-todo-count (count todos)
        active-todo-count (->> todos (filter (complement :completed)) count)]

    [todomvc-header :on-new-todo #(dispatch [:add-todo %])]
    [:section.todoapp #_(print visible-todos)
     [:header.header
      [:h1 "todos"]
      [:input.new-todo
       {:placeholder "What needs to be done?"
        :autofocus   true
        :on-key-down #(if (= (.-keyCode %) 13)
                       (let [value (.-target.value %)]
                         (set! (.-target.value %) "")
                         (dispatch [:add-todo value])))}]]
     [:section.main
      [:input.toggle-all
       {:type      "checkbox"
        :checked   (zero? active-todo-count)
        :on-change #(dispatch
                     [:set-todo-completed-for-all (.-target.checked %)])}]

      [:label {:for "toggle-all"}
       "Mark all as complete"]
      [:ul.todo-list
       (map-indexed
         (fn [index todo]
           ^{:key (:id todo)}
           [todomvc-item {:todo todo :=> dispatch}])
         visible-todos)]]


     ;; --- component: todomvc-footer --------------------------------
     [:footer.footer
      [:span.todo-count
       [:strong active-todo-count] " item left"]            ;; TODO (item vs items)
      [:ul.filters>li
       [(if (= active-filter :all) :a.selected :a)
        {:on-click #(dispatch [:set-filter :all])
         :href     "#"}
        "All"]

       [(if (= active-filter :active)
          :a.selected :a)
        {:on-click #(dispatch [:set-filter :active])
         :href     "#"} "Active"]

       [(if (= active-filter :completed)
          :a.selected :a)
        {:on-click #(dispatch [:set-filter :completed])
         :href     "#"} "Completed"]]

      (if (< active-todo-count total-todo-count)
        [:button.clear-completed
         {:on-click #(dispatch [:clear-completed-todos])}
         "Clear completed"])

      [:footer.info
       [:p " Double-click to edit a todo"]
       [:p " Created by " [:a {:href "http://www.google.com"} "Ralf"]]
       [:p " Part of " [:a {:href "http://todomvc.com"} "TodoMVC"]]]]]))




;; --------------------------------------

(defn todomvc-item-view [props state dispatch]
  (let [todo (:todo props)
        id (:id todo)
        class (str (if (:completed todo) "completed" "active") (if (:in-edit-mode? state) "-editing editing"))
        toggle-edit-mode #(dispatch [:toggle-edit-mode])
        set-todo-text #(dispatch [:set-todo-text id (.-target.value %)]) ik]

    [:li {:key id :class class}
     [:div.view
      [:input.toggle
       {:type      "checkbox"
        :checked   (:completed todo)
        :on-change #(dispatch
                     [:set-todo-completed
                      id
                      (.-checked (.-target %))])}]

      [:label {:on-double-click toggle-edit-mode} (:text todo)]
      [:button.destroy {:on-click #(dispatch [:remove-todo id])}]]
     [:input.edit
      {:type        "text"
       :value       (:text todo)
       :on-change   set-todo-text
       :on-key-down #(if (= (.-keyCode %) 13) (toggle-edit-mode))
       :on-blur     toggle-edit-mode}]]))

(defn todomvc-item-controller [state event]
  (let [[event-key arg] event]
    (case event-key
      :toggle-edit-mode [(update state :in-edit-mode? #(not %)) nil]
      [state event])))


(def todomvc-item
  (bling/create-component-class
    :type-name "todomvc-item"
    :view todomvc-item-view
    :controller todomvc-item-controller
    :initial-state {:in-edit-mode? false} nil))

;; --------------------------------------

(enable-console-print!)

(defn start []
  (bling/mount-component [todomvc-main {:!!! "TODO: Remove debug information when done"}] "root"))
