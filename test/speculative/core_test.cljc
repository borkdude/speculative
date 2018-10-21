(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [speculative.core :as speculative] :reload
   [speculative.test-utils :as tu]))

(stest/instrument)

(deftest =-test
  (is (= 1)))

(deftest filter-test
  (is (filter pos?))
  (is (filter pos? nil))
  (is (= '()  (filter identity nil)))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/filter did not conform to spec"
                        :cljs #"Call to #'cljs.core/filter did not conform to spec")
                     (filter 1))))

(deftest fnil-test
  (is (fnil identity 'lol))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/fnil did not conform to spec"
                        :cljs #"Call to #'cljs.core/fnil did not conform to spec")
                     (fnil 1 1))))

(deftest juxt-text
  (is (= [1 2] ((juxt :a :b) {:a 1 :b 2})))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/juxt did not conform to spec"
                        :cljs #"Call to #'cljs.core/juxt did not conform to spec")
                     (juxt 1 2 3))))

(deftest map-test
  (is (map inc))
  (is (= '(2 3 4) (map inc [1 2 3])))
  (is (= '(1 2) (map (fn [[k v]] v) {:a 1 :b 2})))
  (testing "multiple collections"
    (is (= '(5 7 9)
           (map (fn [a b] (+ a b))
                [1 2 3] [4 5 6]))))
  (testing "nil collection"
    (is (= '() (map identity nil))))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/map did not conform to spec"
                        :cljs #"Call to #'cljs.core/map did not conform to spec")
                     (map 1))))

(deftest merge-test
  (is (merge {}))
  (is (merge {} nil))
  (is (nil? (merge nil)))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/merge did not conform to spec"
                        :cljs #"Call to #'cljs.core/merge did not conform to spec")
                     (merge 1))))


(deftest merge-with-test
  (is (merge-with + {}))
  (is (merge-with + {} nil))
  (is (nil? (merge-with + nil)))
  (is
   (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj #"Call to #'clojure.core/merge-with did not conform to spec"
                        :cljs #"Call to #'cljs.core/merge-with did not conform to spec")
                     (merge-with 1 {}))))

(deftest reduce-test
  (is (reduce + [1 2]))
  (is (reduce + 0 [1 2]))
  (is
   (thrown-with-msg? #?(:clj  clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj  #"Call to #'clojure.core/reduce did not conform to spec"
                        :cljs #"Call to #'cljs.core/reduce did not conform to spec")
                     (reduce 1 [1 2])))
  (is
   (thrown-with-msg? #?(:clj  clojure.lang.ExceptionInfo
                        :cljs js/Error)
                     #?(:clj  #"Call to #'clojure.core/reduce did not conform to spec"
                        :cljs #"Call to #'cljs.core/reduce did not conform to spec")
                     (reduce + 0 1))))

(deftest str-test
  (is (= "" (str nil)))
  (is (= "lolfoo" (str "lol" "foo"))))

;;;; Scratch

(comment
  (t/run-tests))
