(ns speculative.cost
  "Performance cost estimations"
  (:require
   [speculative.core]
   [speculative.cost.popularity :refer [popularity-map]]
   [speculative.test :as t]
   [clojure.pprint :as pprint]
   [clojure.spec-alpha2 :as s]
   [clojure.spec-alpha2.test :as stest]
   [workarounds-1-10-439.core]
   #?(:clj [taoensso.tufte :as tufte :refer [defnp p profiled profile]]
      :cljs [taoensso.tufte :as tufte :refer-macros [defnp p profiled profile]])
   #?(:clj [net.cgrand.macrovich :as macros]))
  #?(:cljs
     (:require-macros
      [net.cgrand.macrovich :as macros]
      [speculative.cost :refer [cost]])))

(defn mean [res]
  (-> res second :id-stats-map :cost :mean))

(def ^:dynamic *iterations* 100000)

(defn unqualify [sym]
  (symbol (name sym)))

(macros/deftime
  (defmacro cost [symbol args]
    `(let [f# (resolve ~symbol)
           args# ~args
           unstrumented#
           (profiled {}
                     (dotimes [_# *iterations*]
                       (p :cost (apply f# args#))))
           instrumented#
           (t/with-instrumentation ~symbol
             (profiled {}
                       (dotimes [_# 100000]
                         (p :cost (apply f# args#)))))
           cost# (int (/ (mean instrumented#)
                         (mean unstrumented#)))
           popularity# (get popularity-map (unqualify ~symbol))]
       {:fdef ~symbol
        :cost cost#
        :popularity popularity#
        :penalty (and cost# popularity# (* cost# popularity#))})))

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
  (pprint/print-table [:fdef :penalty :cost :popularity]
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
