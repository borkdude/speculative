(ns speculative.core-test
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is]]
   [speculative.core :as speculative] :reload))

(deftest map-test
  (stest/instrument `clojure.core/map)
  (is (map inc))
  (is (= '(2 3 4) (map inc [1 2 3])))
  (is (= '(1 2) (map (fn [[k v]] v) {:a 1 :b 2})))
  (is
   (thrown-with-msg? clojure.lang.ExceptionInfo
                     #"Call to #'clojure.core/map did not conform to spec"
                     (map 1)))
  (stest/unstrument `clojure.core/map))


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


;;;; Scratch

(comment
  (t/run-tests))
