(ns apl-step-by-step.read
  (:require [instaparse.core :as insta]
            [apl-step-by-step.symbols :as symbols] 
            [clojure.string]
            [clojure.walk]
            [cljs.reader]
            [cljs.test :refer [deftest is]]))

;; generate productions for functions and operators

(def dyadic-functions
  (->> symbols/dyadic-symbols
    (map (fn [[name sym]] (str name " = (parens | identifier | array) <w>? <'" sym "'> <w>? (dyadic-function | value)")))
    (clojure.string/join "\n")))

(def monadic-functions
  (->> symbols/monadic-symbols
    (map (fn [[name sym]] (str name " = <'" sym "'> <w>? value")))
    (clojure.string/join "\n")))

(def function-parsers
  (str "function-identifier = " 
    (clojure.string/join " | " 
     (map (fn [s] (str "'" s "'")) (vals symbols/dyadic-symbols)))))

(def operators
  (->> symbols/operator-symbols
       (map (fn [[name sym]] (str name " = function-identifier <w>? <'" sym "'> <w>? value")))
       (clojure.string/join "\n")))


(def grammar
  (str "<start> = expr <w>?
         <expr> = assign | value | function | index
         <parens> = <'('> expr <')'>
         function = <'{'> <w>? value <w>? <'}'>
         left-arg = <'" symbols/left-arg "'>
         right-arg = <'" symbols/right-arg "'>
         <value> = parens | operator | dyadic-function | monadic-function | identifier | array\n"
       
       (str "<dyadic-function> = " (clojure.string/join " | " (keys symbols/dyadic-symbols)) "\n")
       dyadic-functions "\n"
       
       (str "<monadic-function> = " (clojure.string/join " | " (keys symbols/monadic-symbols)) "\n")
       monadic-functions "\n"
       
       (str "<operator> = " (clojure.string/join " | " (keys symbols/operator-symbols)) "\n")
       operators "\n"
       function-parsers "\n"
       
        "array = scalar (<' '+> scalar)*
         assign = identifier <w>? <'" symbols/assign "'> <w>? expr
         <scalar> = number | identifier 
         index = value <w>? <'['> <w>? value <w>? <']'>
         identifier = #'[A-z]\\w*' | left-arg | right-arg
         number = #'\\d+(\\.\\d+)?'
         w = ' '+"))

(def parse
  (instaparse.core/parser grammar))

(deftest test-parse-array
  (is (=
       (first (parse "3 4")) 
       [:array [:number "3"] [:number "4"]])))

(deftest test-parse-add
  (is (=
       (first (parse "3 4 + 4"))
       [:add [:array [:number "3"] [:number "4"]] [:array [:number "4"]]])))

(defn node-to-ast [node]
   (if (vector? node)
       (case (first node)
         :number (cljs.reader/read-string (second node))
         :array (vec (rest node))
         node)
       node))

(def to-ast 
  (partial clojure.walk/postwalk node-to-ast))

(defn replace-common-chars [input]
  (-> input
    (clojure.string/replace "<-" "←")
    (clojure.string/replace "-" "−")))

(defn read [input]
  (let [parsed (parse (replace-common-chars input))]
     (if (map? parsed)
       {:error parsed}
       (first (to-ast parsed)))))

(deftest test-read-array
  (is (=
       (read "3 4") 
       [3 4])))

(deftest test-read-add
  (is (=
       (read "3 4 + 4")
       [:add [3 4] [4]])))

;; reader functions for going from sexp representation to same ast as apl grammar
(defn convert-sexp-node [node]
  (cond
    (and (list? node) (= (first node) :assign))
    [:assign [:identifier (name (second node))] (convert-sexp-node (nth node 2))]

    (and (list? node) (= (first node) :reduce))
    [:reduce [:function-identifier (name (second node))] (convert-sexp-node (nth node 2))]
    
    (list? node)
    (into [] (map convert-sexp-node node))

    (symbol? node)
    (keyword node)

    :else node))

(defn read-sexp [input]
  (->> input
    cljs.reader/read-string
    (clojure.walk/postwalk convert-sexp-node)))

