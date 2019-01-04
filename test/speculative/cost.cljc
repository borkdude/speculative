(ns speculative.cost
  "Performance cost estimations"
  (:refer-clojure :exclude [time simple-benchmark])
  (:require
   [speculative.core]
   [speculative.cost.popularity :refer [popularity-map]]
   [respeced.test :as t]
   [clojure.pprint :as pprint]
   [speculative.impl :as impl]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
  #?(:cljs
     (:require-macros [speculative.cost :refer [cost time]])))

(def ^:dynamic *iterations* 100000)

(defn unqualify [sym]
  (symbol (name sym)))

(impl/deftime

  (defmacro time
    [expr]
    (impl/? :clj
       `(let [start# (. System (nanoTime))
              ret# ~expr]
          {:ret ret#
           :ms (/ (double (- (. System (nanoTime)) start#)) 1000000.0)})
       :cljs
       `(let [start# (cljs.core/system-time)
              ret# ~expr]
          {:ret ret#
           :ms (- (cljs.core/system-time) start#)})))

  (defmacro cost [symbol args]
    `(let [f# (resolve ~symbol)
           args# ~args
           unstrumented#
           (:ms (time (dotimes [i# *iterations*]
                        (apply f# args#))))
           instrumented#
           (t/with-instrumentation ~symbol
             (:ms (time (dotimes [i# *iterations*]
                          (apply f# args#)))))
           slowdown# (int (/ instrumented#
                         unstrumented#))
           popularity# (get popularity-map (unqualify ~symbol))]
       {:fdef ~symbol
        :slowdown slowdown#
        :popularity popularity#
        :penalty (and slowdown# popularity# (* slowdown# popularity#))})))

(defn costs []
  [(cost `count [(list 1 2 3 4)])]
  [(cost `= [1 2 3])
   (cost `/ [1 2 3])
   #?(:clj (cost `apply [identity 1 []]))
   (cost `assoc [{:a 1} :b 2])
   (cost `count [(list 1 2 3 4)])
   (cost `every? [pos? (range 10)])
   (cost `filter [pos? (range 10)])
   (cost `first [[1 2 3]])
   (cost `fnil [pos? 0])
   (cost `get [{:a 1} :a])
   (cost `juxt [:a :b])])

(defn print-cost-table []
  (pprint/print-table [:fdef :slowdown :popularity :penalty]
                      (sort-by (comp (fnil - 0) :penalty)
                               (costs))))

(defn parse-int [s]
  (when s
    #?(:cljs (js/parseInt s)
       :clj (Integer/parseInt s))))

(defn -main [& args]
  (let [i (or (parse-int (first args))
              *iterations*)]
    (binding [*iterations* i]
      (println "Calculating costs with" i "iterations")
      (print-cost-table))))

;;;; Scratch

(comment
  (cost `= [1 2 3])
  (pprint/print-table {:a 1})

  )
