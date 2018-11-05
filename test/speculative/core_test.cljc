(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [is deftest testing]]
   [clojure.spec.gen.alpha :as gen]
   [speculative.core :as speculative] :reload
   [speculative.test :refer [with-instrumentation
                             with-unstrumentation
                             throws
                             check]]
   [speculative.test-utils :refer [gentest]]
   [workarounds-1-10-439.core]))

(deftest =-test
  (is (check `= [1]))
  (is (check `= [1 1]))
  (gentest `=)
  (with-instrumentation `=
    (throws `= (=))))

(deftest division-test
  ;; Note: / is inlined when used with more than one argument
  ;; With `check` it's not inlined.
  (is (check `/ [1]))
  (is (check `/ [1 2]))
  (is (check `/ [1 2 3]))
  (with-instrumentation `/
    (testing "Divide by zero, no spec error"
      #?(:cljs (is (= ##Inf (/ 0)))
         :clj (is (thrown-with-msg? java.lang.ArithmeticException
                                    #"Divide by zero"
                                    (/ 0)))))
    (throws `/ (apply / nil))
    (throws `/ (apply / ['a]))))

(deftest apply-test
  (is (= 21 (check `apply [+ 1 2 3 [4 5 6]])))
  (is (= 0 (check `apply [+ nil])))
  #?(:clj (with-instrumentation `apply
            (throws `apply (apply + 1 2 3 4)))
     :cljs nil ;; maximum call stack exceeded
     ))

(deftest assoc-test
  (is (check `assoc [nil 'lol 'lol]))
  (is (check `assoc [{} 'lol 'lol 'bar 'lol]))
  (is (check `assoc [[] 0 'lol]))
  (with-instrumentation `assoc
    (throws `assoc (assoc 'lol 'lol 'lol))
    (throws `assoc (assoc {} 'lol))))

(deftest count-test
  (is (check `count [nil]))
  (is (check `count [[1]]))
  (is (check `count [{:a 1}]))
  (is (check `count [(into-array [1 2])]))
  (is (check `count ["abc"]))
  (with-instrumentation `count
    (throws `count (apply count [1]))))

(deftest every?-test
  (is (check `every? [pos? nil]))
  (is (check `every? [identity nil]))
  (with-instrumentation `every?
    (throws `every? (every? 1 []))))

(deftest filter-test
  (is (check `filter [pos?]))
  (is (check `filter [pos? nil]))
  (is (= '()  (check `filter [identity nil])))
  (with-instrumentation `filter
    (throws `filter (filter 1))))

(deftest first-test
  (is (nil? (check `first [nil])))
  (is (= 1 (check `first ['(1 2 3)])))
  (with-instrumentation `first
    (throws `first (first 1))))

(deftest fnil-test
  (is (check `fnil [identity 'lol]))
  (with-instrumentation `fnil
    (throws `fnil (fnil 1 1))))

(deftest get-test
  (is (= 'foo (check `get [#{'foo} 'foo 'bar])))
  (is (nil? (check `get [1 1])))
  (with-instrumentation `get
    (throws `get (get))))

(deftest juxt-text
  (is (= [1 2] ((check `juxt [:a :b]) {:a 1 :b 2})))
  (with-instrumentation `juxt
    (throws `juxt (juxt 1 2 3))))

(deftest map-test
  (is (check `map [inc]))
  (is (= '(2 3 4) (check `map [inc [1 2 3]])))
  (is (= '(1 2) (check `map [(fn [[k v]] v) {:a 1 :b 2}])))
  (testing "multiple collections"
    (is (= '(5 7 9)
           (check `map [(fn [a b] (+ a b))
                        [1 2 3] [4 5 6]]))))
  (testing "nil collection"
    (is (= '() (check `map [identity nil]))))
  (with-instrumentation `map
    (throws `map (map 1))))

(deftest map-entry-test
  (let [mes (gen/sample (s/gen ::speculative/map-entry))]
    (is (seq mes))
    (is (every? #(= 2 (count %)) mes))))

(deftest merge-test
  (is (check `merge [{}]))
  (is (nil? (check `merge [])))
  (is (check `merge [{} nil]))
  (is (nil? (check `merge [nil])))
  (with-instrumentation `merge
    (throws `merge (merge 1))))

(deftest merge-with-test
  (is (check `merge-with [+ {}]))
  (is (nil? (check `merge-with [+])))
  (is (check `merge-with [+ {} nil]))
  (is (nil? (check `merge-with [+ nil])))
  (with-instrumentation `merge-with
    (throws `merge-with (merge-with 1))))

(deftest not-any-test
  (is (check `not-any? [pos? nil]))
  (is (= true (check `not-any? [identity nil])))
  (with-instrumentation `not-any?
    (throws `not-any? (not-any? 1 []))))

(deftest not-every-test
  (is (false? (check `not-every? [pos? nil])))
  (is (check `not-every? [pos? [-1 1]]))
  (with-instrumentation `not-every?
    (throws `not-every? (not-every? 1 []))))

(deftest range-test
  (is (check `range []))
  (is (check `range [1]))
  (is (check `range [1 10]))
  (is (check `range [10 0 -1]))
  (is (check `range [1.1 2.2 3.3]))
  (with-instrumentation `range
    (throws `range (range 'lol))
    (throws `range (range 0 1 2 3))))

(deftest partial-test
  (is (check `partial [identity]))
  (is (check `partial [+ 1 2 3]))
  (with-instrumentation `partial
    (throws `partial (partial 1))))

(deftest reduce-test
  (is (check `reduce [+ [1 2]]))
  (is (check `reduce [+ 0 [1 2]]))
  (with-instrumentation `reduce
    (throws `reduce (reduce 1 [1 2]))
    (throws `reduce (reduce + 0 1))))

(deftest remove-test
  (is (check `remove [pos?]))
  (is (check `remove [pos? nil]))
  (is (= '() (check `remove [identity nil])))
  (with-instrumentation `remove
    (throws `remove (remove 1))))

(deftest reset!-test
  (is (check `reset! [(atom nil) 1]))
  (with-instrumentation `reset!
    (throws `reset! (reset! 1 (atom nil)))))

(deftest some-test
  (is (not (check `some [pos? nil])))
  (is (nil? (check `some [identity nil])))
  (with-instrumentation `some
    (throws `some (some 1 []))))

(deftest str-test
  (is (= "" (check `str [nil])))
  (is (= "lolfoo" (check `str ["lol" "foo"])))
  (gentest `str)
  (with-instrumentation `str
    ;; there's really no way to make str crash, is there?
    ))

(deftest swap!-test
  (is (nil? (check `swap! [(atom nil) identity])))
  (is (nil? (check `swap! [(atom nil) (fn [x y]) 1])))
  (with-instrumentation `swap!
    (throws `swap! (swap! 1 identity))
    (throws `swap! (swap! (atom nil) 1) (+ 1 2 3))))

;;;; Scratch

(comment
  (t/run-tests)
  )
