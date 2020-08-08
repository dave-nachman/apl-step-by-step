(ns apl-step-by-step.symbols)

;; TODO: add more symbols

(def dyadic-symbols 
 {"add" "+"
  "subtract" "−"
  "multiply" "×"
  "divide" "÷"
  "drop" "↓"
  "deal" "?"
  "reshape" "⍴"})

(def monadic-symbols
 {"roll" "?"
  "range" "⍳"
  "grade-up" "⍋"
  "grade-down" "⍒"})

(def operator-symbols
 {"reduce" "/"})


(def assign "←")

;; TODO: implement evaluation for left and right arg
(def left-arg "α")
(def right-arg "ω")

(def all-symbols
  (merge 
   dyadic-symbols 
   monadic-symbols
   operator-symbols
   {"assign" assign 
    "left arg" left-arg
    "right arg" right-arg}))
