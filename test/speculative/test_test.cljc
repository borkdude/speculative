(ns speculative.test-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [speculative.test :refer [with-instrumentation
                             with-unstrumentation
                             throws
                             check]]))

(defn foo [n]
  "ret")

(deftest test-test
  (testing "check"

    (s/fdef foo
      :args (s/cat :n number?)
      :ret number?)

    (is (thrown-with-msg?
         #?(:clj clojure.lang.ExceptionInfo
            :cljs ExceptionInfo)
         #"Specification-based check failed"
         (check `foo [1])))

    (s/fdef foo
      :args (s/cat :n number?)
      :ret string?)

    (is (= "ret" (check `foo [1]))))

  (testing "with-(i/u)nstrumentation"

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
      (is (= "ret" (foo "not a number"))))))

;;;; Scratch

(comment
  (t/run-tests)
  )
