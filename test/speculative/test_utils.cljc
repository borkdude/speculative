(ns speculative.test-utils
  "Test utils."
  (:require
   [clojure.test]
   [speculative.impl :refer [deftime]])
  #?(:cljs
     (:require-macros
      [speculative.test-utils :refer [check]])))

(defn planck-env? []
  #?(:cljs (exists? js/PLANCK_EXIT_WITH_VALUE)
     :clj false))

(deftime
  (defmacro check
    "wrapper for gentest with num-tests defaulting to 50 and test
  assertion."
    ([sym]
     `(let [stc-result#
            (respeced.test/check ~sym nil {:num-tests 50})]
        (clojure.test/is (respeced.test/successful? stc-result#))))
    ([sym opts]
     `(let [stc-result#
            (respeced.test/check ~sym ~opts {:num-tests 50})]
        (clojure.test/is (respeced.test/successful? stc-result#))))))

;;;; Scratch

(comment
  (t/run-tests)
  )
