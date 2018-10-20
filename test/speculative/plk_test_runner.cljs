(ns speculative.plk-test-runner
  (:require
   [speculative.core-test]
   [cljs.test]
   [planck.core]))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (when-not (cljs.test/successful? m)
    (planck.core/exit 1)))

(defn -main [& args]
  (cljs.test/run-tests 'speculative.core-test))
