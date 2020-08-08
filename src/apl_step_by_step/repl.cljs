(ns apl-step-by-step.repl
  (:require [apl-step-by-step.read :refer [read]]
            [apl-step-by-step.eval :refer [eval-apl]]
            [apl-step-by-step.print :refer [pprint]]
            [cljs.test :refer [deftest is run-tests]]))
        
(defn read-eval [input env]
  (eval-apl (read input) env))

(def read-eval-print (comp pprint read-eval))

(defn repl
  ([] (repl (atom {})))
  ([env] 
   (fn [input]
    (try
      (read-eval-print input env)
      (catch js/Object e
        (str e))))))

(deftest add-read-eval
  (is (= (read-eval "2 2 + 3" (atom {})) [5 5])))

(run-tests)


