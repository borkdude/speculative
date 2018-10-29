;; run with:

;; clj -Srepro -Sdeps '{:deps {org.clojure/clojurescript {:git/url "https://github.com/clojure/clojurescript" :sha "4fb83eff87cc456600a3fd21c111e99a41c61285"}}}' -m cljs.main -re node -i CLJS_2949_test.cljs

(ns CLJS-2949-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]))

(defn foo [n]
  "ret")

(s/fdef foo
  :args (s/cat :n number?)
  :ret number?)

(deftest repro-test
  (testing "unstrument in finally works"
    (is (= "oops"
           (try
             (stest/instrument `foo)
             (foo "string")
             (catch js/Error e "oops")
             (finally
               (stest/unstrument `foo))))))
  (testing "should be unstrumented after try/catch/finally"
    (let [ret (try (foo "string")
                   (catch js/Error e "not-ret"))]
      (is (= "ret" ret)) ;; FAILS
      (testing "if ret wasn't ret, then foo was still instrumented. unstrumenting..."
        (is (seq (s/unstrument `foo))) ;; FAILS
        ))))

(t/run-tests)
