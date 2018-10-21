(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [speculative.core :as speculative] :reload
   [speculative.test-utils :as tu]
   #?(:clj [net.cgrand.macrovich :as macros])
   [clojure.string :as str])
  #?(:cljs
     (:require-macros [net.cgrand.macrovich :as macros]
                      [speculative.core-test :refer [throws]])))

(macros/deftime
  (defmacro throws [symbol & body]
    `(let [msg#
           (macros/case :clj (try ~@body
                                  (catch clojure.lang.ExceptionInfo e#
                                    (str e#)))
                        :cljs (try ~@body
                                   (catch js/Error e#
                                     (str e#))))]
       (clojure.test/is (str/includes?
            msg#
            (str "Call to " (resolve ~symbol)
                 " did not conform to spec"))))))

(deftest =-test
  (tu/with-instrumentation `=
    (is (= 1))))

(deftest every?-test
  (tu/with-instrumentation `every?
    (is (every? pos? nil))
    (is (= true  (every? identity nil)))
    (throws `every? (every? 1 []))))

(deftest filter-test
  (tu/with-instrumentation `filter
    (is (filter pos?))
    (is (filter pos? nil))
    (is (= '()  (filter identity nil)))
    (throws `filter (filter 1))))

(deftest first-test
  (tu/with-instrumentation `first
    (is (nil? (first nil)))
    (is 1 (first '(1 2 3)))
    (throws `first (first 1))))

(deftest fnil-test
  (tu/with-instrumentation `fnil
    (is (fnil identity 'lol))
    (throws `fnil (fnil 1 1))))

(deftest juxt-text
  (tu/with-instrumentation `juxt
    (is (= [1 2] ((juxt :a :b) {:a 1 :b 2})))
    (throws `juxt (juxt 1 2 3))))

(deftest map-test
  (tu/with-instrumentation `map
    (is (map inc))
    (is (= '(2 3 4) (map inc [1 2 3])))
    (is (= '(1 2) (map (fn [[k v]] v) {:a 1 :b 2})))
    (testing "multiple collections"
      (is (= '(5 7 9)
             (map (fn [a b] (+ a b))
                  [1 2 3] [4 5 6]))))
    (testing "nil collection"
      (is (= '() (map identity nil))))
    (throws `map (map 1))))

(deftest merge-test
  (tu/with-instrumentation `merge
    (is (merge {}))
    (is (merge {} nil))
    (is (nil? (merge nil)))
    (throws `merge (merge 1))))

(deftest merge-with-test
  (tu/with-instrumentation `merge-with
    (is (merge-with + {}))
    (is (merge-with + {} nil))
    (is (nil? (merge-with + nil)))
    (throws `merge-with (merge-with 1))))

(deftest not-any-test
  (tu/with-instrumentation `not-any?
    (is (not-any? pos? nil))
    (is (= true  (not-any? identity nil)))
    (throws `not-any? (not-any? 1 []))))

(deftest reduce-test
  (tu/with-instrumentation `reduce
    (is (reduce + [1 2]))
    (is (reduce + 0 [1 2]))
    (throws `reduce (reduce 1 [1 2]))
    (throws `reduce (reduce + 0 1))))

(deftest remove-test
  (tu/with-instrumentation `remove
    (is (remove pos?))
    (is (remove pos? nil))
    (is (= '()  (remove identity nil)))
    (throws `remove (remove 1))))

(deftest some-test
  (tu/with-instrumentation `some
    (is (not (some pos? nil)))
    (is (nil? (some identity nil)))
    (throws `some (some 1 []))))

(deftest str-test
  (tu/with-instrumentation `str
    (is (= "" (str nil)))
    (is (= "lolfoo" (str "lol" "foo")))))

;;;; Scratch

(comment
  (t/run-tests))
