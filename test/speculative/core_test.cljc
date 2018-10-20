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
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/map did not conform to spec"
                        :cljs #"Call to #'cljs.core/map did not conform to spec")
                     (map 1))
   (stest/unstrument `clojure.core/map)))

(deftest merge-test
  (stest/instrument `clojure.core/merge)
  (is (merge {}))
  (is (merge {} nil))
  (is (nil? (merge nil)))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/merge did not conform to spec"
                        :cljs #"Call to #'cljs.core/merge did not conform to spec")
                     (merge 1)))
  (stest/unstrument `clojure.core/merge))


(deftest filter-test
  (stest/instrument `clojure.core/filter)
  (is (filter pos?))
  (is (filter pos? nil))
  (is (= '()  (filter identity nil)))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/filter did not conform to spec"
                        :cljs #"Call to #'cljs.core/filter did not conform to spec")
                     (filter 1)))
  (stest/unstrument `clojure.core/filter))



;;;; Scratch

(comment
  (t/run-tests))
