(ns speculative.specs-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as t :refer [is deftest testing]]
   [speculative.specs :as ss]
   [respeced.test :as rt :refer [with-instrumentation
                                 with-unstrumentation
                                 caught?
                                 check-call]]
   [speculative.test-utils :refer [check]]
   ;; included for self-hosted cljs
   [workarounds-1-10-439.core]))

(deftest map-entry-test
  (let [mes (gen/sample (s/gen ::ss/map-entry))]
    (is (seq mes))
    (is (every? #(= 2 (count %)) mes))))

(deftest regexp-test
  (let [res (gen/sample (s/gen ::ss/regexp))]
    (is (seq res))
    (is (every? ss/regexp? res))))

(deftest any-test
  (is (s/valid? ::ss/any :clojure.spec.alpha/invalid)))

;;;; Scratch

(comment
  (t/run-tests))
