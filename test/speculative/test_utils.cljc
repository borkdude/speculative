(ns speculative.test-utils
  "Test utils."
  (:require
   [clojure.test]
   [speculative.impl :refer [deftime]]
   #?(:cljs [goog.object :as gobj]))
  #?(:cljs
     (:require-macros
      [speculative.test-utils :refer [check]])))

(defn planck-env? []
  #?(:cljs (exists? js/PLANCK_EXIT_WITH_VALUE)
     :clj false))

(defn parse-int [s]
  #?(:clj (Integer/parseInt s)
     :cljs (js/parseInt s)))

(def num-tests
  (or
   (when-let
       [nt
        #?(:clj (System/getenv "NUM_TESTS")
           :cljs (if (planck-env?)
                   (gobj/get (js/PLANCK_GETENV) "NUM_TESTS")
                   (.. js/process -env -NUM_TESTS)))]
     (parse-int nt))
   50))

(deftime
  (defmacro check
    "wrapper for gentest with num-tests defaulting to 50 and test
  assertion."
    ([sym]
     `(let [stc-result#
            (respeced.test/check ~sym nil {:num-tests num-tests})]
        (clojure.test/is (respeced.test/successful? stc-result#))))
    ([sym opts]
     `(let [stc-result#
            (respeced.test/check ~sym ~opts {:num-tests num-tests})]
        (clojure.test/is (respeced.test/successful? stc-result#))))))

;;;; Scratch

(comment
  (t/run-tests)
  )
