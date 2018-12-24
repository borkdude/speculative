(ns speculative.instrument-test
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [respeced.test :refer [caught?]]
   [speculative.instrument :as instrument]
   ;; included for self-hosted cljs
   [workarounds-1-10-439.core]
   ))

(deftest instrument-test
  (testing "speculative specs should be instrumentable and unstrumentable"
    (let [spec-count #?(:clj 51 :cljs 47)
          instrumented (instrument/instrument)
          unstrumented (instrument/unstrument)]
      (is (= spec-count (count instrumented)))
      ;; <= is a temporary workaround for CLJS-2975
      (is (<= spec-count (count unstrumented))))))

(deftest fixture-test
  (testing "without instrumentation"
    (is (some? (fnil 1 1))))

  (testing "with instrumentation"
    (is (caught? `fnil
                 (instrument/fixture
                  #(fnil 1 1))))))
