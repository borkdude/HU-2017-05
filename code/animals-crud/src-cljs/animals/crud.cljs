(ns animals.crud
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as r]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce animals-state (r/atom #{}))

;; initial call to get animals from server
(go (let [response
          (<! (http/get "/api/animals"))
          data (:body response)]
      (reset! animals-state (set data))))

;;; crud operations

(defn remove-by-id [s id]
  (set (remove #(= id (:id %)) s)))

(defn add-animal! [a]
  (go (let [response
            (<! (http/post "/api/animals" {:edn-params
                                       a}))]
        (swap! animals-state conj a))))

(defn remove-animal! [a]
  (go (let [response
            (<! (http/delete (str "/api/animals/"
                                  (:id a))))]
        (swap! animals-state remove-by-id (:id a)))))
 
(defn update-animal! [{:keys [id name species] :as a}]
  (go (let [response
            (<! (http/put (str "/api/animals/" id)
                          {:edn-params {:name name :species species}}))
            status (:status response)]
        (swap! animals-state
               (fn [old-state]
                 (conj
                  (remove-by-id old-state id)
                  a))))))

;;; end crud operations

(defn editable-input [atom key]
  (if (:editing? @atom)
    [:input {:type     "text"
             :value    (get @atom key)
             :on-change (fn [e] (swap! atom
                                       assoc key
                                       (.. e -target -value)))}]
    [:p (get @atom key)]))

(defn input-valid? [atom]
  (and (seq (-> @atom :name))
       (seq (-> @atom :species))))

(defn animal-row [{:keys [id name species] :as initial-animal}]
  (let [row-state (r/atom {:editing? false
                           :name name
                           :species species})
        current-animal (fn []
                         (assoc initial-animal
                                :name (:name @row-state)
                                :species (:species @row-state)))]
    (fn []
      [:tr
       [:td [editable-input row-state :name]]
       [:td [editable-input row-state :species]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? row-state))
              :on-click (fn []
                         (when (:editing? @row-state)
                           (update-animal! (current-animal)))
                         (swap! row-state update-in [:editing?] not))}
             (if (:editing? @row-state) "Save" "Edit")]]
       [:td [:button.btn.pull-right.btn-danger
             {:on-click #(remove-animal! (current-animal))}
             "\u00D7"]]])))

(defn animal-form []
  (let [initial-form-values {:name     ""
                             :species  ""
                             :editing? true}
        form-input-state (r/atom initial-form-values)]
    (fn []
      [:tr
       [:td [editable-input form-input-state :name]]
       [:td [editable-input form-input-state :species]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? form-input-state))
              :on-click  (fn []
                           (add-animal! (dissoc @form-input-state
                                                :editing?))
                          (reset! form-input-state initial-form-values))}
             "Add"]]])))

(defn animals []
  [:div
   [:table.table.table-striped
    [:thead
     [:tr
      [:th "Name"] [:th "Species"] [:th ""] [:th ""]]]
    [:tbody
     (map (fn [a]
            ^{:key (str "animal-row-" (:id a))}
            [animal-row a])
          (sort-by :name @animals-state))
     [animal-form]]]])

(r/render-component [animals]
                    (js/document.getElementById "app"))
