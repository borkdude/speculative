(ns speculative.instrument-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [speculative.instrument :as instrument]
   [speculative.test :refer [throws]]
   ;; included for self-hosted cljs
   [workarounds-1-10-439.core]))

(deftest fixture-test
  (testing "without instrumentation"
    (is (some? (fnil 1 1))))

  (testing "with instrumentation"
    (throws `fnil (instrument/fixture (fn []
                                        (fnil 1 1))))))
