(ns apl-step-by-step.eval
  (:require [cljs.test :refer [deftest is]]
            [clojure.set]
            [apl-step-by-step.symbols :refer [dyadic-symbols]]))

(declare eval-one)

(defn dyadic [op]
  (fn [[a b] _]
    (cond
      (= (count a) (count b))
      (->> (map vector a b)
           (map (fn [[x y]] (op x y)))
           (into []))

      (= (count a) 1)
      (into [] (map #(op (first a) %) b))

      (= (count b) 1)
      (into [] (map #(op % (first b)) a))

      :else (throw (js/Error. "Cannot apply this operator to arrays of these lengths")))))

(defn roll [[a] _]
  (into [] (take 1 (shuffle a))))

(defn deal [[a b] _]
  (into [] (take (first a) (shuffle b))))

;; fns end in hyphens if they would conflict with built-ins
(defn range- [[a] _]
  (into [] (range 1 (+ (first a) 1))))

(defn drop- [[a b] _]
  (into [] (drop (first a) b)))

(defn grade-up [[a] _]
  (into [] (map (fn [v] (inc (.indexOf a v))) (sort a))))

(def grade-down (comp #(into [] %) reverse grade-up))

(def dyadic-lookup 
  (clojure.set/map-invert dyadic-symbols))

(defn reduce- [[sym xs] env]
  (reduce (fn [m n] (eval-one [(keyword (dyadic-lookup sym)) [m] [n]] env)) xs))

(defn index- [[a b] _]
  (into [] (map #(nth a (dec %)) b)))

(def add (dyadic +))
(def subtract (dyadic -))
(def multiply (dyadic *))
(def divide- (dyadic /))

(deftest add-same-length
   (is (= (add [1 2 3] [1 2 3])
          [2 4 6])))

(deftest add-apply-a
   (is (= (add [1] [1 2 3])
          [2 3 4])))

(deftest add-apply-b
   (is (= (add [1 2 3] [1])
          [2 3 4])))

(defn assign [[[_ ident] value] env]
  (swap! env assoc (keyword ident) (eval-one value env))
  nil)

(defn identifier [[name] env]
  (if (contains? @env (keyword name))
    (@env (keyword name))
    (throw (js/Error. (str "identifier not found: " name)))))

;; TODO: create a nicer pattern for this
(def fns 
   {:add add
    :subtract subtract
    :multiply multiply
    :divide divide-
    :assign assign
    :roll roll
    :deal deal
    :grade-up grade-up
    :grade-down grade-down
    :range range-
    :drop drop-
    :index index-
    :reduce reduce-})

(def silent-fns
   {:identifier identifier 
    :function-identifier (fn [n _] (first n))})


(defn eval-one [node env]
  (cond
    (and (map? node) (node :error))
    node

    (and (vector? node) (= (first node) :assign))
    (assign (rest node) env)

    (and (vector? node) (keyword? (first node)) (get fns (first node)))
    (let [result ((fns (first node)) (map #(eval-one % env) (rest node)) env)]
      (swap! env assoc :--inter-results (conj (@env :--inter-results) [node result]))
      result)
    
    (and (vector? node) (keyword? (first node)))
    ((silent-fns (first node)) (map #(eval-one % env) (rest node)) env)
    
    (vector? node)
    ;; TODO: revisit this logic
    (flatten (into [] (map #(eval-one % env) node)))

     ;; TODO: replace this 
    :else node))


(defn eval-apl [node env]
  (swap! env assoc :--inter-results [])
  (eval-one node env))
