;; The goal of this exercise is to build a collapsible panel.
;; The template code basically looks like this: 

(ns student.collapsible-panel
  (:require [reagent.core :as reagent]
            [re-frame.core :as reframe]))


(reframe/reg-event-db
  :initialize
  (fn [_ _]
    {}))


(defn ui []
  [:div
   [:h1 "Hello World"]])


(defonce _init (reframe/dispatch-sync [:initialize]))
(reagent/render [ui] (js/document.getElementById "student-container"))

                                                                        
;; The first assignment is to create a test component to show up on screen:      

(defn counter []                                                                
  (let [*counter-state (reagent/atom 0)]                                        
    (js/setInterval #(swap! *counter-state inc) 1000)                           
    (fn [] [:div @*counter-state])))


(defn ui []
  [:div
    [counter]])


;; A panel with 'title' and 'child' is created next:

(defn panel [title child]
  [:div
    [:div title]
    [:div child]])

  
(defn ui []
  [:div
   [panel "Collapsible panel" [counter]]])


;; To create the click behavior, some state is needed. Therefore, the panel
;; function should return a function which should be called when rendering.
;; Now the panel function is called during construction and the function it
;; returns is going to be called every time the panel should be rendered. Both
;; functions should have the same arguments.


(defn panel [title child]
  (let [*panel-state (reagent/atom {:open false})]
    (fn [title child]
      [:div
        [:div 
          {:on-click #(swap! *panel-state update :open not)}
          title]
        [:div (when (:open @*panel-state) child)]])))


;; But in this setup the component is remounted every time which resets the 
;; timer every time. So let's hide the child instead of completely removing it
;; every time:


(defn panel [title child]
  (let [*panel-state (reagent/atom {:open false})]
    (fn [title child]
      [:div
        [:div 
          {:on-click #(swap! *panel-state update :open not)}
          title]
        [:div 
          {:style
            {:visibility (when-not (:open @*panel-state) "hidden")}}
          child]])))


;; Now add some styling to it:

(defn panel [title child]
  (let [*panel-state (reagent/atom {:open false})]
    (fn [title child]
      [:div
        [:div 
          {:on-click #(swap! *panel-state update :open not)
           :style 
             {:background-color "#ddd"
              :padding "0 1em"}}
          title]
        [:div 
          {:style
            {:background-color "#eee"
             :padding "0 1em"
             :visibility (when-not (:open @*panel-state) "hidden")}}
          child]])))


;; And a '+' and '-' to emphasize the 'collapsibility':

(defn panel [title child]
  (let [*panel-state (reagent/atom {:open false})]
    (fn [title child]
      (let [open? (:open @*panel-state)]
        [:div
          [:div 
            {:on-click #(swap! *panel-state update :open not)
             :style 
               {:background-color "#ddd"
                :padding "0 1em"}}
            [:div 
              {:style {:float "right"}}
              (if open? "-" "+")]
            title]
          [:div 
            {:style
              {:background-color "#eee"
               :padding "0 1em"
               :visibility (when-not open? "hidden")}}
            child]]))))

;; Finally, the panel local-state is moved to the database. This enables
;; components outside the panel to control its behavior. For instance when the
;; app has a 'collapse all' button. The final code looks like this:

(ns student.collapsible-panel
  (:require [reagent.core :as reagent]
            [re-frame.core :as reframe]))


(reframe/reg-event-db
  :initialize
  (fn [_ _]
    {}))


(reframe/reg-event-db
  :toggle-panel
  (fn [db [_ id]]
    (update-in db [:panels id] not)))


(reframe/reg-sub
  :panel-state
  (fn [db [_ id]]
    (get-in db [:panels id])))


(defn counter []
  (let [*counter-state (reagent/atom 0)]
    (js/setInterval #(swap! *counter-state inc) 1000)
    (fn [] [:div @*counter-state])))


(defn panel [id title child]                                                                                    
    (fn [id title child]                                                           
      (let [open? @(reframe/subscribe [:panel-state id])]                                        
        [:div                                                                   
          [:div                                                                 
            {:on-click #(reframe/dispatch [:toggle-panel id])                   
             :style                                                             
               {:background-color "#ddd"                                        
                :padding "0 1em"}}                                              
            [:div                                                               
              {:style {:float "right"}}                                         
              (if open? "-" "+")]                                               
            title]                                                              
          [:div                                                                 
            {:style                                                             
              {:background-color "#eee"                                         
               :padding "0 1em"                                                 
               :visibility (when-not open? "hidden")}}                          
            child]])))


(defn ui []
  [:div
   [panel :time-panel "Time panel" [counter]]])


(defonce _init (reframe/dispatch-sync [:initialize]))
(reagent/render [ui] (js/document.getElementById "student-container"))
