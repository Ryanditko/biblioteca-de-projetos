(ns iniciante.calculadora-simples.script
  (:require [clojure.string :as str]))

(defn tokenize [expr]
  (->> expr
       (str/replace " " "")
       (re-seq #"\d+(\.\d+)?|[+\-*/]")
       (map #(if (re-matches #"\d+(\.\d+)?" %)
               (Double/parseDouble %)
               (first %)))))

(defn resolve-high-precedence [tokens]
  (loop [result []
         [t & more] tokens]
    (if (nil? t)
      result
      (if (#{\* \/} t)
        (let [prev (peek result)
              next (first more)
              calc (if (= t \*)
                     (* prev next)
                     (/ prev next))]
          (recur (conj (pop result) calc) (rest more)))
        (recur (conj result t) more)))))

(defn resolve-low-precedence [tokens]
  (loop [acc (first tokens)
         [op n & more] (rest tokens)]
    (if (nil? op)
      acc
      (recur (case op
               \+ (+ acc n)
               \- (- acc n))
             more))))

(defn calcular [expr]
  (-> expr tokenize resolve-high-precedence resolve-low-precedence))
