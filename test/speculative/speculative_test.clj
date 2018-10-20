(ns speculative.speculative-test
  (:require [clojure.spec.test.alpha :as stest]
            [clojure.test :as t]
            [speculative.core :as speculative] :reload))

(t/deftest merge-test
  (stest/instrument `clojure.core/merge)
  (t/is (merge {}))
  (t/is (merge {} nil))
  (t/is (nil? (merge nil)))
  (t/is
    (thrown-with-msg? clojure.lang.ExceptionInfo
      #"Call to #'clojure.core/merge did not conform to spec"
      (merge 1)))
  (stest/unstrument `clojure.core/merge))
