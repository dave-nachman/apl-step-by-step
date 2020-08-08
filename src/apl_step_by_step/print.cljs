(ns apl-step-by-step.print
  (:require [clojure.set]
            [clojure.string]
            [apl-step-by-step.symbols :refer [dyadic-symbols monadic-symbols operator-symbols all-symbols assign]]))

(def all-symbols-lookup 
  (clojure.set/map-invert all-symbols))

;; pretty-print node into a map with useful information for rendering
(defn rich-print [node]
  (cond
    (and (vector? node) (keyword? (first node)) (dyadic-symbols (name (first node))))
    [(rich-print (second node)) " " {:type "function" :helper (name (first node)) :value (dyadic-symbols (name (first node)))} " " (rich-print (nth node 2))]
    
    (and (vector? node) (keyword? (first node)) (monadic-symbols (name (first node))))
    [{:type "function" :helper (name (first node)) :value (monadic-symbols (name (first node)))} (rich-print (second node))]
    
    (and (vector? node) (keyword? (first node)) (operator-symbols (name (first node))))
    [(rich-print (second (second node))) {:type "operator" :helper (name (first node)) :value (operator-symbols (name (first node)))} (rich-print (nth node 2))]
    
    (and (vector? node) (= (first node) :identifier))
    [{:type "identifier" :helper "identifier" :value (second node)}]
    
    (and (vector? node) (= (first node) :assign))
    [{:type "identifier" :helper "identifier" :value (second node)} " " {:type "assign" :helper "assign" :value assign} " " (rich-print (nth node 2))]    
     
    (and (vector? node) (= (first node) :index))
    [(rich-print (second node)) "[" (rich-print (nth node 2)) "]"]    
    
    (sequential? node)
    (into [] (interpose " " (map rich-print node)))
    
    (nil? node)
    [{:type "nothing" :value "<nothing>"}]

    :else [{:type "value" :helper (all-symbols-lookup (str name)) :value (str node)}]))

(defn rich->str [node]
  (cond
    (map? node) 
    (:value node)
    
    (sequential? node)
    (clojure.string/join " " (map rich->str node))

    :else (str node)))

(defn pprint [node]
  (rich->str (rich-print node)))


(defn print-sexp [node]
  (cond
    (and (vector? node) (keyword? (first node)) (dyadic-symbols (name (first node))))
    (str "(" (name (first node)) " " (print-sexp (second node)) " " (print-sexp (nth node 2)) ")")
    
    (and (vector? node) (keyword? (first node)) (monadic-symbols (name (first node))))
    (str "(" (name (first node)) " " (print-sexp (second node)) ")")
    
    (and (vector? node) (keyword? (first node)) (operator-symbols (name (first node))))
    (str "(" (name (first node)) " " (print-sexp (second (second node))) " " (print-sexp (nth node 2)) ")")

    (and (vector? node) (keyword? (first node)) (= (first node) :index))
    (str "(index " (print-sexp (second node)) " " (print-sexp (nth node 2)) ")")
     
    (and (vector? node) (keyword? (first node)) (= (first node) :assign))
    (str "(assign " (print-sexp (second (second node))) " " (print-sexp (nth node 2)) ")")    
    
    (and (vector? node) (keyword? (first node)) (= (first node) :identifier))
    (name (second node))
    
    (sequential? node)
    (str "[" (clojure.string/join " " (map print-sexp node)) "]")

    (nil? node)
    "nil"

    :else (str node)))
