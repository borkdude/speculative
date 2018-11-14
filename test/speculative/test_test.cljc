(ns speculative.test-test
  (:require
   #?(:clj [clojure.spec-alpha2 :as s]
      :cljs [clojure.spec.alpha :as s])
   #?(:clj [clojure.spec-alpha2.test :as stest]
      :cljs [clojure.spec.test.alpha :as stest])
   [clojure.test :as t :refer [deftest is testing]]
   [speculative.test :refer [with-instrumentation
                             with-unstrumentation
                             throws
                             check-call
                             check
                             successful?
                             test-check-kw]]
   ;; included for self-hosted cljs
   #_[workarounds-1-10-439.core]))

(defn foo [n]
  "ret")

(deftest check-call-test
  (s/fdef foo
    :args (s/cat :n number?)
    :ret number?)

  (is (thrown-with-msg?
       #?(:clj clojure.lang.ExceptionInfo
          :cljs ExceptionInfo)
       #"Specification-based check failed"
       (check-call `foo [1])))

  (s/fdef foo
    :args (s/cat :n number?)
    :ret string?)

  (is (= "ret" (check-call `foo [1]))))

(deftest instrument-test
  (s/fdef foo
    :args (s/cat :n number?)
    :ret string?)

  (testing "manual instrument"
    (stest/instrument `foo)
    (throws `foo (foo "not a number")))

  (testing "no instrumentation"
    (with-unstrumentation `foo
      (is (= "ret" (foo "not a number")))))

  (testing "manual instrumentation is restored"
    (throws `foo (foo "not a number")))

  (testing "undo manual instrumentation"
    (stest/unstrument `foo)
    (is (= "ret" (foo "not a number"))))

  (testing "with instrumentation"
    (with-instrumentation `foo
      (throws `foo (foo "not a number"))))

  (testing "no instrumentation"
    (is (= "ret" (foo "not a number")))))

(deftest check-test
  (s/fdef foo
    :args (s/cat :n number?)
    :ret string?)
  (testing "successful?"
    (is (not (successful? [])))
    (is (successful? [{(test-check-kw "ret") {:pass? true}}]))
    (is (not (successful? [{(test-check-kw "ret") {:pass? false}}]))))
  #_(testing "check"
    (let [ret (check `foo nil {:num-tests 42})
          rets (map (test-check-kw "ret") ret)]
      (is (successful? ret))
      (is (every? #(= 42 (:num-tests %)) rets)))))

;;;; Scratch

(comment
  (t/run-tests)
  )
