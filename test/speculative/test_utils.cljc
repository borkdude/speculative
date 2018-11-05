(ns speculative.test-utils
  "Test utils."
  (:require
   [clojure.string :as str]
   [clojure.test]
   [clojure.test.check]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [speculative.test]
   [workarounds-1-10-439.core]
   #?(:clj [net.cgrand.macrovich :as macros]))
  #?(:cljs
     (:require-macros
      [net.cgrand.macrovich :as macros]
      [speculative.test-utils :refer [gentest]])))

(defn planck-env? []
  #?(:cljs (exists? js/PLANCK_EXIT_WITH_VALUE)
     :clj false))

(macros/deftime

  (defmacro gentest
    "wrapper for gentest with num-tests defaulting to 50 and test
  assertion."
    [sym]
    `(let [stc-result#
           (speculative.test/gentest ~sym nil {:num-tests 50})]
       (clojure.test/is (speculative.test/success? stc-result#)))))

;;;; Scratch

(comment
  (t/run-tests)
  )
