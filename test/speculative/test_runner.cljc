(ns speculative.test-runner
  (:require
   [speculative.test-test]
   [speculative.core-test]
   [speculative.test-utils :refer [planck-env?]]
   [clojure.test]))

(defn exit
  "Exit with the given status."
  [status]
  #?(:cljs
     (let [exit-fn (cond (exists? js/process)
                         #(.exit js/process %)
                         (planck-env?)
                         js/PLANCK_EXIT_WITH_VALUE)]
       (exit-fn status))
     :clj (do
            (shutdown-agents)
            (System/exit status))))

#?(:cljs
   (defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
     (if-not (cljs.test/successful? m)
       (exit 1)
       (exit 0)))
   :clj
   (defmethod clojure.test/report :summary [m]
     (clojure.test/with-test-out
       (println "\nRan" (:test m) "tests containing"
                (+ (:pass m) (:fail m) (:error m)) "assertions.")
       (println (:fail m) "failures," (:error m) "errors."))
     (if-not (clojure.test/successful? m)
       (exit 1)
       (exit 0))))

(defn -main [& args]
  (clojure.test/run-tests 'speculative.test-test
                          'speculative.core-test))
