(ns speculative.cljs-test-runner
  (:require
   [speculative.core-test]
   [cljs.test]))

(defn exit
  "Exit with the given status."
  [status]
  (if (exists? js/process)
    (.exit js/process status)
    (js/PLANCK_EXIT_WITH_VALUE status)))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (when-not (cljs.test/successful? m)
    (exit 1)))

(defn -main [& args]
  (cljs.test/run-tests 'speculative.core-test))
