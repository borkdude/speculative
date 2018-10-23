(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [speculative.core :as speculative] :reload
   [speculative.test-utils :refer [with-instrumentation
                                   throws]]))

(deftest =-test
  (with-instrumentation `=
    (is (= 1))))

(deftest division-test
  (with-instrumentation `/
    (is (= 1 (/ 1)))
    #?(:cljs (is (= ##Inf (/ 0)))
       :clj (is (thrown-with-msg? clojure.lang.ExceptionInfo
                                  #"Call to #'clojure.core// did not conform to spec"
                                  (/ 0))))
    #?(:clj (is (thrown-with-msg? clojure.lang.ExceptionInfo
                                  #"Call to #'clojure.core// did not conform to spec"
                                  (/ 'a))))

    #?(:cljs (is (= ##Inf (/ 1 0 )))
       :clj (is (thrown-with-msg? clojure.lang.ExceptionInfo
                                  #"Call to #'clojure.core// did not conform to spec"
                                  (apply / [1 0]))))))

#?(:clj
   (deftest apply-test
     (with-instrumentation `apply
       (is (apply + 1 2 3 [4 5 6]))
       (is (apply + nil))
       (throws `apply (apply + 1 2 3 4))))
   :cljs nil
   ;; waiting for https://dev.clojure.org/jira/browse/CLJS-2942
   )

(deftest count-test
  (with-instrumentation `count
    (is (count nil))
    (is (count [1]))
    (is (count {:a 1}))
    (is (count (into-array [1 2])))
    (is (count "abc"))
    (throws `count (apply count [1]))))

(deftest every?-test
  (with-instrumentation `every?
    (is (every? pos? nil))
    (is (= true  (every? identity nil)))
    (throws `every? (every? 1 []))))

(deftest filter-test
  (with-instrumentation `filter
    (is (filter pos?))
    (is (filter pos? nil))
    (is (= '()  (filter identity nil)))
    (throws `filter (filter 1))))

(deftest first-test
  (with-instrumentation `first
    (is (nil? (first nil)))
    (is (= 1 (first '(1 2 3))))
    (throws `first (first 1))))

(deftest fnil-test
  (with-instrumentation `fnil
    (is (fnil identity 'lol))
    (throws `fnil (fnil 1 1))))

(deftest juxt-text
  (with-instrumentation `juxt
    (is (= [1 2] ((juxt :a :b) {:a 1 :b 2})))
    (throws `juxt (juxt 1 2 3))))

(deftest map-test
  (with-instrumentation `map
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
  (with-instrumentation `merge
    (is (merge {}))
    (is (merge {} nil))
    (is (nil? (merge nil)))
    (throws `merge (merge 1))))

(deftest merge-with-test
  (with-instrumentation `merge-with
    (is (merge-with + {}))
    (is (merge-with + {} nil))
    (is (nil? (merge-with + nil)))
    (throws `merge-with (merge-with 1))))

(deftest not-any-test
  (with-instrumentation `not-any?
    (is (not-any? pos? nil))
    (is (= true  (not-any? identity nil)))
    (throws `not-any? (not-any? 1 []))))

(deftest reduce-test
  (with-instrumentation `reduce
    (is (reduce + [1 2]))
    (is (reduce + 0 [1 2]))
    (throws `reduce (reduce 1 [1 2]))
    (throws `reduce (reduce + 0 1))))

(deftest remove-test
  (with-instrumentation `remove
    (is (remove pos?))
    (is (remove pos? nil))
    (is (= '()  (remove identity nil)))
    (throws `remove (remove 1))))

(deftest reset!-test
  (with-instrumentation `reset!
    (is (reset! (atom nil) 1))
    (throws `reset! (reset! 1 (atom nil)))))

(deftest some-test
  (with-instrumentation `some
    (is (not (some pos? nil)))
    (is (nil? (some identity nil)))
    (throws `some (some 1 []))))

(deftest str-test
  (with-instrumentation `str
    (is (= "" (str nil)))
    (is (= "lolfoo" (str "lol" "foo")))))

(deftest swap!-test
  (with-instrumentation `swap!
    (throws `swap! (swap! 1 identity))
    (throws `swap! (swap! (atom nil) 1))
    (is (nil? (swap! (atom nil) identity)))
    (is (nil? (swap! (atom nil) (fn [x y]) 1)))))

;;;; Scratch

(comment
  (t/run-tests))
