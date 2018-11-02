(ns speculative.test-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [clojure.test.check]
   [speculative.test-utils :refer [planck-env?]]
   [speculative.test :refer [with-instrumentation
                             with-unstrumentation
                             throws
                             check
                             gentest
                             success?
                             test-check-kw]]))

(defn foo [n]
  "ret")

(deftest check-test
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


(deftest gentest-test
  (s/fdef foo
    :args (s/cat :n number?)
    :ret string?)
  (testing "success?"
    (is (not (success? [])))
    (is (success? [{(test-check-kw "ret") {:pass? true}}]))
    (is (not (success? [{(test-check-kw "ret") {:pass? false}}]))))
  (when-not (planck-env?)
    (testing "gentest"
      (let [ret (gentest `foo nil {:num-tests 42})
            rets (map (test-check-kw "ret") ret)]
        (is (success? ret))
        (is (every? #(= 42 (:num-tests %)) rets))
        ))))

;;;; Scratch

(comment
  (t/run-tests)
  )
