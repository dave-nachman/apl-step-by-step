(ns apl-step-by-step.ui
    (:require [reagent.core :as reagent :refer [atom]]
              [apl-step-by-step.repl :refer [repl]]
              [apl-step-by-step.symbols :as symbols]
              [apl-step-by-step.print :refer [pprint rich-print print-sexp]]
              [apl-step-by-step.read :refer [read read-sexp]]))
             
(enable-console-print!)

(defn input-as-sexp [input]
  (let [result (->> input
                 read
                 print-sexp)]
   (if (clojure.string/starts-with? result "{:error")
     ""
     result)))

(defn input-from-sexp [input]
  (->> input
    read-sexp
    pprint))

(defonce app-state 
  (atom {:show-trace false 
         :show-sexp false
         :show-inline-names false
         :input "2 + 2" 
         :input-sexp (input-as-sexp "2 + 2") 
         :result nil :env (atom {})}))

(defn run [input]
   (swap! app-state assoc :result ((repl (@app-state :env)) input)))

(def red-color "#d05066")

(defn render-rich-print [node]
  (cond
    (map? node)
    [:span 
      {:style 
       {:display :inline-grid 
        :text-align :center} 
       :title (node :helper)} 
      (node :value) 
      [:span 
       {:style 
        {:color red-color
         :padding "2px 4px" 
         :display (if (@app-state :show-inline-names) "inline-block" "none")}} 
       (node :helper)]]
    
    (vector? node)
    [:span (map render-rich-print node)]

    :else node))

(def pprint-color "rgb(230, 230, 230)")

(defn code-result [v]
  (if (> (count v) 0)
    [:span {:style {:background-color pprint-color :border-radius 4 :padding "4px 6px" :border "1px solid rgb(210, 210, 210)"}} v]
    [:span]))

(defn render-inner-result [[parse result]] 
  [:div 
   {:style {:margin "12px 0 18px 0" :padding "6px 0"}}
   [:div
    {:style {:padding "8px 16px 8px 0" :display :inline-block}}
    (code-result
     (map render-rich-print (rich-print parse)))
    " → "  
    (code-result (pprint result))]
   [:div {:style {:padding "8px 0" :opacity 0.5 :display (if (@app-state :show-sexp) "inline-block" "none")}}
    (code-result
     (print-sexp parse))
    " → "  
    (code-result (print-sexp result))]])


(defn set-sexp-value [input]
  (swap! app-state assoc :input-sexp input)
  (swap! app-state assoc :input (input-from-sexp input)))

(defn set-value [input]
  (swap! app-state assoc :input input)
  (swap! app-state assoc :input-sexp (input-as-sexp input)))


(defn section [title & rest]
  [:div 
    {:style {:min-height 40 :border-bottom "1px solid #ccc" :padding-bottom 12}} 
    [:h3 title] 
    rest])


(defn render-env []
  (let [env (deref (@app-state :env))
        clean-env (dissoc env :--inter-results)]
    (if (empty? clean-env)
      [:div {:key "none"} "(no variables have been assigned)"]
      [:div {:key "env"} (map (fn [[k v]] [:div {:key k} [:span {:style {:min-width 48}} k] [:span " " symbols/assign " " (pprint v)]]) clean-env)])))

(defn trace-section []
  (if (@app-state :show-trace)
     (section "Evaluation steps"
              [:div (map render-inner-result ((deref (@app-state :env)) :--inter-results))])
     nil))

(defn input-box []
  [:div {:style {:display :inline-block}}
    [:div {:class "input-label"} "APL: "]
    [:input {:value (@app-state :input)
             :on-change #(set-value (-> % .-target .-value))}]])


(defn left-column []
  [:div {:style {:flex 2 :padding-right 32}}
   (input-box)
   [:button {:on-click #(run (@app-state :input))} "Run"]
   (if (@app-state :show-sexp) [:div {:class "input-label"} "s-expr:"] nil)
   [:input {:style {:opacity 0.5 :width 200 :margin-left 12 :display (if (@app-state :show-sexp) "inline-block" "none")}
            :value (@app-state :input-sexp)
            :on-change #(set-sexp-value (-> % .-target .-value))}]
   (section "Result"
            [:div {:style {:margin-top 12}} (code-result (@app-state :result))])
   (trace-section)])
   

(defn option [key label]
  [:div {:style {:font-size 14}}
     [:label label]
     [:input 
      {:value (@app-state key) 
       :on-click #(swap! app-state assoc key (not (@app-state key))) :type "checkbox"}]])


(defn options-section []
  [:div {:style {:display :flex :flex-direction :column}}
   (option :show-sexp "Show s-expressions?")
   (option :show-trace "Show evaluation steps")
   (option :show-inline-names "Show inline names on steps")])
    
(defn symbol-button [[name sym]] 
  [:button
   ;; TODO: add to current cursor position (if any) rather than at end  
   {:key name :on-click #(swap! app-state assoc :input (str (@app-state :input) sym))} 
   (str sym " (" name ")")])

(defn set-and-eval [input]
   (set-value input)
   (run input))

(defn render-example [[k v]]
  [:div
    {:key k}
    [:h5 k]
    [:div 
      {:style 
       {:background-color "#ddd" 
        :padding "4px 6px" 
        :border-radius 4
        :cursor "pointer"}
       :on-click #(set-and-eval v)}
      [:span {:style {:color "#aaa"}} "▶  "]
      [:span v]]])
       


(def symbols-section
  [:a {:href "https://en.wikipedia.org/wiki/APL_syntax_and_symbols#Monadic_functions" :target "__blank"}
    (section "Symbols"
      [:div (map symbol-button symbols/all-symbols)])])

(defn environment-section []
  (deref (@app-state :env))
  (section "Environment"
            (render-env)))

(defn middle-column []
  [:div {:style {:flex 1 :padding-right 32}} 
    (options-section)
    symbols-section 
    (environment-section)])

(def examples 
  {"add two numbers" "2 + 2"
   "addition over array" "2 + 1 2 3"
   "sum array and add" "+/⍳4 + 2"
   "assign a variable" "x ← 1 2 3"
   "evaluate a variable" "x + 2"
   "deal add reduce range add" "2 ? 1 2 3 + +/⍳2 + 2"})


(def examples-section
  (section "Examples"
   (map render-example examples)))

(defn right-column []
  [:div {:style {:flex 1 :padding-top 58}} 
    examples-section])

(defn app [] 
  [:div {:style {:display :flex :flex-direction "column"}}
    [:div {:style {:display :flex :align-items :center :justify-content :space-between :background-color "#f2f2f2" :padding "0 24px" :margin-bottom "12px"}} 
      [:h3 "APL step-by-step"]
      [:a {:href "https://github.com/dave-nachman/apl-step-by-step" :target "__blank"} [:img {:src "github.png" :style {:height 32 :opacity 0.5}}]]]
    [:div {:style {:padding "0 24px" :display :flex}} 
      (left-column)
      (middle-column)  
      (right-column)]])

(reagent/render-component [app]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

