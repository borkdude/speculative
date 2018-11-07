(ns speculative.test-utils
  "Test utils."
  (:require
   [clojure.test]
   [speculative.test :refer [deftime]])
  #?(:cljs
     (:require-macros
      [speculative.test-utils :refer [gentest]])))

(defn planck-env? []
  #?(:cljs (exists? js/PLANCK_EXIT_WITH_VALUE)
     :clj false))

(deftime
  (defmacro gentest
    "wrapper for gentest with num-tests defaulting to 50 and test
  assertion."
    [sym]
    `(let [stc-result#
           (speculative.test/gentest ~sym nil {:num-tests 50})]
       (clojure.test/is (speculative.test/successful? stc-result#)))))

;;;; Scratch

(comment
  (t/run-tests)
  )
