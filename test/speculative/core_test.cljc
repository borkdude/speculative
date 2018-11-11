(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as t :refer [is deftest testing]]
   [clojure.set :as set]
   [speculative.core :as speculative]
   [speculative.test :refer [with-instrumentation
                             with-unstrumentation
                             throws
                             check-call]]
   [speculative.test-utils :refer [check]]
   ;; included for self-hosted cljs
   [workarounds-1-10-439.core]))

(deftest instrument-all-test
  (testing "all specs should be instrumentable and unstrumentable"
    (let [spec-count #?(:clj 25 :cljs 24)
          instrumented (stest/instrument)
          instrumented (filter #(= #?(:clj "clojure.core"
                                      :cljs "cljs.core")
                                   (namespace %))
                               instrumented)]
      (is (= spec-count (count instrumented)))
      (is (set/subset?
           (set instrumented)
           (set (stest/unstrument)))))))

(deftest =-test
  (is (check-call `= [1]))
  (is (check-call `= [1 1]))
  (check `=)
  (with-instrumentation `=
    (throws `= (=))))

(deftest division-test
  ;; Note: / is inlined when used with more than one argument
  ;; With `check` it's not inlined.
  (is (check-call `/ [1]))
  (is (check-call `/ [1 2]))
  (is (check-call `/ [1 2 3]))
  (with-instrumentation `/
    (testing "Divide by zero, no spec error"
      #?(:cljs (is (= ##Inf (/ 0)))
         :clj (is (thrown-with-msg? java.lang.ArithmeticException
                                    #"Divide by zero"
                                    (/ 0)))))
    (throws `/ (apply / nil))
    (throws `/ (apply / ['a]))))

(deftest apply-test
  (is (= 21 (check-call `apply [+ 1 2 3 [4 5 6]])))
  (is (= 0 (check-call `apply [+ nil])))
  #?(:clj (with-instrumentation `apply
            (throws `apply (apply + 1 2 3 4)))
     :cljs nil ;; maximum call stack exceeded
     ))

(deftest assoc-test
  (is (check-call `assoc [nil 'lol 'lol]))
  (is (check-call `assoc [{} 'lol 'lol 'bar 'lol]))
  (is (check-call `assoc [[] 0 'lol]))
  (with-instrumentation `assoc
    (throws `assoc (assoc 'lol 'lol 'lol))
    (throws `assoc (assoc {} 'lol))))

(deftest count-test
  (is (check-call `count [nil]))
  (is (check-call `count [[1]]))
  (is (check-call `count [{:a 1}]))
  (is (check-call `count [(into-array [1 2])]))
  (is (check-call `count ["abc"]))
  (with-instrumentation `count
    (throws `count (apply count [1]))))

(deftest every?-test
  (is (check-call `every? [pos? nil]))
  (is (check-call `every? [identity nil]))
  (with-instrumentation `every?
    (throws `every? (every? 1 []))))

(deftest filter-test
  (is (check-call `filter [pos?]))
  (is (check-call `filter [pos? nil]))
  (is (= '()  (check-call `filter [identity nil])))
  (with-instrumentation `filter
    (throws `filter (filter 1))))

(deftest first-test
  (is (nil? (check-call `first [nil])))
  (is (= 1 (check-call `first ['(1 2 3)])))
  (with-instrumentation `first
    (throws `first (first 1))))

(deftest fnil-test
  (is (check-call `fnil [identity 'lol]))
  (with-instrumentation `fnil
    (throws `fnil (fnil 1 1))))

(deftest get-test
  (is (= 'foo (check-call `get [#{'foo} 'foo 'bar])))
  (is (nil? (check-call `get [1 1])))
  (with-instrumentation `get
    (throws `get (get))))

(deftest juxt-text
  (is (= [1 2] ((check-call `juxt [:a :b]) {:a 1 :b 2})))
  (with-instrumentation `juxt
    (throws `juxt (juxt 1 2 3))))

(deftest map-test
  (is (check-call `map [inc]))
  (is (= '(2 3 4) (check-call `map [inc [1 2 3]])))
  (is (= '(1 2) (check-call `map [(fn [[k v]] v) {:a 1 :b 2}])))
  (testing "multiple collections"
    (is (= '(5 7 9)
           (check-call `map [(fn [a b] (+ a b))
                        [1 2 3] [4 5 6]]))))
  (testing "nil collection"
    (is (= '() (check-call `map [identity nil]))))
  (with-instrumentation `map
    (throws `map (map 1))))

(deftest map-entry-test
  (let [mes (gen/sample (s/gen ::speculative/map-entry))]
    (is (seq mes))
    (is (every? #(= 2 (count %)) mes))))

(deftest merge-test
  (is (check-call `merge [{}]))
  (is (nil? (check-call `merge [])))
  (is (check-call `merge [{} nil]))
  (is (nil? (check-call `merge [nil])))
  (with-instrumentation `merge
    (throws `merge (merge 1))))

(deftest merge-with-test
  (is (nil? (check-call `merge-with [+])))
  (is (nil? (check-call `merge-with [+ nil])))
  (is (= {:a 1} (check-call `merge-with [+ {:a 1}])))
  (is (= {:a 1} (check-call `merge-with [+ {:a 1} nil])))
  (is (= {:a 3}
         (check-call `merge-with [+ {:a 1} [(first {:a 2})]])))
  (with-instrumentation `merge-with
    (throws `merge-with (merge-with 1))
    ;; the following is no longer allowed in CLJS, see CLJS-2943
    (throws `merge-with (merge-with + {:a 1} [[:a 2]]))))

(deftest not-any-test
  (is (check-call `not-any? [pos? nil]))
  (is (= true (check-call `not-any? [identity nil])))
  (with-instrumentation `not-any?
    (throws `not-any? (not-any? 1 []))))

(deftest not-every-test
  (is (false? (check-call `not-every? [pos? nil])))
  (is (check-call `not-every? [pos? [-1 1]]))
  (with-instrumentation `not-every?
    (throws `not-every? (not-every? 1 []))))

(deftest range-test
  (is (check-call `range []))
  (is (check-call `range [1]))
  (is (check-call `range [1 10]))
  (is (check-call `range [10 0 -1]))
  (is (check-call `range [1.1 2.2 3.3]))
  (with-instrumentation `range
    (throws `range (range 'lol))
    (throws `range (range 0 1 2 3))))

(deftest partial-test
  (is (check-call `partial [identity]))
  (is (check-call `partial [+ 1 2 3]))
  (with-instrumentation `partial
    (throws `partial (partial 1))))

(deftest reduce-test
  (is (check-call `reduce [+ [1 2]]))
  (is (check-call `reduce [+ 0 [1 2]]))
  (with-instrumentation `reduce
    (throws `reduce (reduce 1 [1 2]))
    (throws `reduce (reduce + 0 1))))

(deftest remove-test
  (is (check-call `remove [pos?]))
  (is (check-call `remove [pos? nil]))
  (is (= '() (check-call `remove [identity nil])))
  (with-instrumentation `remove
    (throws `remove (remove 1))))

(deftest reset!-test
  (is (check-call `reset! [(atom nil) 1]))
  (with-instrumentation `reset!
    (throws `reset! (reset! 1 (atom nil)))))

(deftest some-test
  (is (not (check-call `some [pos? nil])))
  (is (nil? (check-call `some [identity nil])))
  (with-instrumentation `some
    (throws `some (some 1 []))))

(deftest str-test
  (is (= "" (check-call `str [nil])))
  (is (= "lolfoo" (check-call `str ["lol" "foo"])))
  (check `str)
  (with-instrumentation `str
    ;; there's really no way to make str crash, is there?
    ))

(deftest swap!-test
  (is (nil? (check-call `swap! [(atom nil) identity])))
  (is (nil? (check-call `swap! [(atom nil) (fn [x y]) 1])))
  (with-instrumentation `swap!
    (throws `swap! (swap! 1 identity))
    (throws `swap! (swap! (atom nil) 1) (+ 1 2 3))))

;;;; Scratch

(comment
  (t/run-tests)
  )
