(ns speculative.instrument-test
  (:require
   #?(:clj [clojure.spec-alpha2 :as s]
      :cljs [clojure.spec.alpha :as s])
   #?(:clj [clojure.spec-alpha2.test :as stest]
      :cljs [clojure.spec.test.alpha :as stest])
   [clojure.test :as t :refer [deftest is testing]]
   [respeced.test :refer [caught?]]
   [speculative.instrument :as i]))

(deftest instrument-test
  (testing "speculative specs should be instrumentable and unstrumentable"
    (is (let [instrumented (i/instrument)
              unstrumented (i/unstrument)]
          (println "instrumented:" (count instrumented))
          (println "unstrumented:" (count unstrumented))
          true)))
  (testing "it should be safe to call (stest/instrument) after requiring
  speculative instrument"
    (is
     (let [stest-instrumented (stest/instrument)]
       (println "stest-instrumented:" (count stest-instrumented))
       (stest/unstrument)
       true)))
  (testing "environment specific tests"
    (i/instrument)
    #?(:clj
       (testing "next is not blacklisted on clj"
         (is (caught? `next (apply next [1])))))
    #?(:cljs
       (testing "hash-map is not blacklisted on cljs"
         (is (caught? `hash-map (apply hash-map [1])))))))
