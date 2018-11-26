(ns speculative.test-runner
  (:require
   [speculative.test-test]
   [speculative.core-test]
   [speculative.set-test]
   [speculative.test :refer [planck-env?]]
   [clojure.test :as t :refer [run-tests]]
   #?(:clj [patch.clj-2443])
   [clojure.test]))

(defn exit
  "Exit with the given status."
  [status]
  #?(:cljs
     (when-let
         [exit-fn
          (cond
            ;; node
            (exists? js/process)
            #(.exit js/process %)
            ;; nashorn
            (exists? js/exit)
            js/exit
            ;; planck
            (planck-env?)
            js/PLANCK_EXIT_WITH_VALUE)]
       (exit-fn status))
     :clj (do
            (shutdown-agents)
            (System/exit status))))

#?(:cljs
   (do
     (defmethod cljs.test/report [:cljs.test/default :begin-test-var] [m]
       ;; for debugging:
       ;; (println ":begin-test-var" (cljs.test/testing-vars-str m))
       )
     (defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
       (if-not (cljs.test/successful? m)
         (exit 1)
         (exit 0))))
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
  (run-tests 'speculative.test-test
             'speculative.core-test
             'speculative.set-test))

#?(:cljs (set! *main-cli-fn* -main))
