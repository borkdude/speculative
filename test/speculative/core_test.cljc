(ns speculative.core-test
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is]]
   [speculative.core :as speculative] :reload
   [clojure.spec.alpha :as s]))

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


(deftest merge-with-test
  (stest/instrument `clojure.core/merge-with)
  (is (merge-with + {}))
  (is (merge-with + {} nil))
  (is (nil? (merge-with + nil)))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/merge-with did not conform to spec"
                        :cljs #"Call to #'cljs.core/merge-with did not conform to spec")
                     (merge-with 1 {})))
  (stest/unstrument `clojure.core/merge-with))


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

(deftest fnil-test
  (stest/instrument `clojure.core/fnil)
  (is (fnil identity 'lol))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/fnil did not conform to spec"
                        :cljs #"Call to #'cljs.core/fnil did not conform to spec")
                     (fnil 1 1)))
  (stest/unstrument `clojure.core/fnil))

(deftest =-test
  (stest/instrument `clojure.core/=)
  (is (= 1))
  (stest/unstrument `clojure.core/=))


;;;; Scratch

(comment
  (t/run-tests))
