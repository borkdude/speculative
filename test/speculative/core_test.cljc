(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as t :refer [is deftest testing]]
   [clojure.set :as set]
   [speculative.specs :as ss]
   [speculative.instrument]
   [respeced.test :as rt :refer [with-instrumentation
                                 with-unstrumentation
                                 caught?
                                 check-call]]
   [speculative.test-utils :refer [check]]
   ;; included for self-hosted cljs
   [workarounds-1-10-439.core]))

(deftest =-test
  (is (check-call `= [1]))
  (is (check-call `= [1 1]))
  (check `=)
  (with-instrumentation `=
    (is (caught? `= (=)))))

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
    (is (caught? `/ (apply / nil)))
    (is (caught? `/ (apply / ['a])))))

(deftest apply-test
  (is (= 21 (check-call `apply [+ 1 2 3 [4 5 6]])))
  (is (= 0 (check-call `apply [+ nil])))
  #?(:clj (with-instrumentation `apply
            (is (caught? `apply (apply + 1 2 3 4))))
     :cljs nil ;; maximum call stack exceeded
     ))

(deftest assoc-test
  (is (check-call `assoc [nil 'lol 'lol]))
  (is (check-call `assoc [{} 'lol 'lol 'bar 'lol]))
  (is (check-call `assoc [[] 0 'lol]))
  (with-instrumentation `assoc
    (is (caught? `assoc (assoc 'lol 'lol 'lol)))
    (is (caught? `assoc (assoc {} 'lol)))))

(deftest count-test
  (is (check-call `count [nil]))
  (is (check-call `count [[1]]))
  (is (check-call `count [{:a 1}]))
  (is (check-call `count [(into-array [1 2])]))
  (is (check-call `count ["abc"]))
  (with-instrumentation `count
    (is (caught? `count (apply count [1])))))

(deftest every?-test
  (is (check-call `every? [pos? nil]))
  (is (check-call `every? [identity nil]))
  (with-instrumentation `every?
    (is (caught? `every? (every? 1 [])))))

(deftest filter-test
  (is (check-call `filter [pos?]))
  (is (check-call `filter [pos? nil]))
  (is (= '()  (check-call `filter [identity nil])))
  (with-instrumentation `filter
    (is (caught? `filter (filter 1)))))

(deftest first-test
  (is (nil? (check-call `first [nil])))
  (is (= 1 (check-call `first ['(1 2 3)])))
  (with-instrumentation `first
    (is (caught? `first (first 1)))))

(deftest fnil-test
  (is (check-call `fnil [identity 'lol]))
  (with-instrumentation `fnil
    (is (caught? `fnil (fnil 1 1)))))

(deftest get-test
  (is (= 'foo (check-call `get [#{'foo} 'foo 'bar])))
  (is (nil? (check-call `get [1 1])))
  (with-instrumentation `get
    (is (caught? `get (get)))))

(deftest juxt-text
  (is (= [1 2] ((check-call `juxt [:a :b]) {:a 1 :b 2})))
  (with-instrumentation `juxt
    (is (caught? `juxt (juxt 1 2 3)))))

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
    (is (caught? `map (map 1)))))

(deftest map-entry-test
  (let [mes (gen/sample (s/gen ::ss/map-entry))]
    (is (seq mes))
    (is (every? #(= 2 (count %)) mes))))

(deftest merge-test
  (is (check-call `merge [{}]))
  (is (nil? (check-call `merge [])))
  (is (check-call `merge [{} nil]))
  (is (nil? (check-call `merge [nil])))
  #?(:clj (is (= {:a 1 :b 2}
                 (merge {:a 1} (java.util.HashMap. {:a 1 :b 2})))))
  (with-instrumentation `merge
    (is (caught? `merge (merge 1)))))

(deftest merge-with-test
  (is (nil? (check-call `merge-with [+])))
  (is (nil? (check-call `merge-with [+ nil])))
  (is (= {:a 1} (check-call `merge-with [+ {:a 1}])))
  (is (= {:a 1} (check-call `merge-with [+ {:a 1} nil])))
  (is (= {:a 3}
         (check-call `merge-with [+ {:a 1} [(first {:a 2})]])))
  #?(:clj
     (testing "second arg is java Map"
       (is (= {:a 2 :b 2})
           (merge-with + {:a 1} (java.util.HashMap. {:a 1 :b 2})))))
  (with-instrumentation `merge-with
    (is (caught? `merge-with (merge-with 1)))
    ;; the following is no longer allowed in CLJS, see CLJS-2943
    (is (caught? `merge-with (merge-with + {:a 1} [[:a 2]])))))

(deftest not-any-test
  (is (check-call `not-any? [pos? nil]))
  (is (= true (check-call `not-any? [identity nil])))
  (with-instrumentation `not-any?
    (is (caught? `not-any? (not-any? 1 [])))))

(deftest not-every-test
  (is (false? (check-call `not-every? [pos? nil])))
  (is (check-call `not-every? [pos? [-1 1]]))
  (with-instrumentation `not-every?
    (is (caught? `not-every? (not-every? 1 [])))))

(deftest range-test
  (is (check-call `range []))
  (is (check-call `range [1]))
  (is (check-call `range [1 10]))
  (is (check-call `range [10 0 -1]))
  (is (check-call `range [1.1 2.2 3.3]))
  (with-instrumentation `range
    ;; (is (range)) ;; doesn't work with advanced: CLJS-2995
    (is (caught? `range (range 'lol)))
    (is (caught? `range (range 0 1 2 3)))))

(deftest partial-test
  (is (check-call `partial [identity]))
  (is (check-call `partial [+ 1 2 3]))
  (with-instrumentation `partial
    (is (caught? `partial (partial 1)))))

(deftest reduce-test
  (is (check-call `reduce [+ [1 2]]))
  (is (check-call `reduce [+ 0 [1 2]]))
  (with-instrumentation `reduce
    (is (caught? `reduce (reduce 1 [1 2])))
    (is (caught? `reduce (reduce + 0 1)))))

(deftest remove-test
  (is (check-call `remove [pos?]))
  (is (check-call `remove [pos? nil]))
  (is (= '() (check-call `remove [identity nil])))
  (with-instrumentation `remove
    (is (caught? `remove (remove 1)))))

(deftest reset!-test
  (is (check-call `reset! [(atom nil) 1]))
  (with-instrumentation `reset!
    (is (caught? `reset! (reset! 1 (atom nil))))))

(deftest some-test
  (is (not (check-call `some [pos? nil])))
  (is (nil? (check-call `some [identity nil])))
  (with-instrumentation `some
    (is (caught? `some (some 1 [])))))

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
    (is (caught? `swap! (swap! 1 identity)))
    (is (caught? `swap! (swap! (atom nil) 1) (+ 1 2 3)))))

(deftest regexp-test
  (let [res (gen/sample (s/gen ::ss/regexp))]
    (is (seq res))
    (is (every? ss/regexp? res))))

(deftest re-pattern-test
  (is (check-call `re-pattern ["s"]))
  (check `re-pattern)
  (with-instrumentation `re-pattern
    (is (caught? `re-pattern (re-pattern 1)))))

#?(:clj
   (deftest re-matcher-test
     (is (check-call `re-matcher [#"s" "s"]))
     (with-instrumentation `re-matcher
       (is (caught? `re-matcher (re-matcher 1 "s")))
       (is (caught? `re-matcher (re-matcher #"s" 1))))
     (check `re-matcher)))

#?(:clj
   (deftest re-groups-test
     (let [non-matching-matcher (re-matcher #"(a)(a)(a)" "bbb")
           single-matching-matcher (re-matcher #"aaa" "aaa")
           groups-matching-matcher (re-matcher #"(a)(a)(a)" "aaa")]
       (.find single-matching-matcher)
       (.find groups-matching-matcher)
       (.find non-matching-matcher)
       (is (thrown? java.lang.IllegalStateException
                    (check-call `re-groups [non-matching-matcher])))
       (testing "returning string"
         (is (check-call `re-groups [single-matching-matcher])))
       (testing "returning seqable of strings"
         (is (check-call `re-groups [groups-matching-matcher])))
       (with-instrumentation `re-groups
         (is (caught? `re-groups (re-groups 1)))))))

(deftest re-seq-test
  (testing "no matches"
    (is (nil? (check-call `re-seq [#"a" "b"]))))
  (testing "one match"
    (is (check-call `re-seq [#"s" "s"])))
  (with-instrumentation `re-seq
    (is (caught? `re-seq (re-seq 1 "s")))
    (is (caught? `re-seq (re-seq #"s" 1))))
  (check `re-seq))

(deftest re-matches-test
  (testing "no matches"
    (is (nil? (check-call `re-matches [#"a" "b"]))))
  (testing "returning string"
    (is (check-call `re-matches [#"hello.*" "hello there"])))
  (testing "returning seqable of string"
    (is (check-call `re-matches [#"(hello.*)" "hello there"])))
  (with-instrumentation `re-matches
    (is (caught? `re-matches (re-matches 1 "s")))
    (is (caught? `re-matches (re-matches #"s" 1))))
  (check `re-matches))

(deftest re-find-test
  #?(:clj (testing "call with matcher"
            (is (check-call `re-find [(re-matcher #"(a)(a)(a)" "aaa")]))))
  (testing "no matches"
    (is (nil? (check-call `re-find [#"a" "b"]))))
  (testing "returning string"
    (is (check-call `re-find [#"hello.*" "hello there"])))
  (testing "returning seqable of string"
    (is (check-call `re-find [#"(hello.*)" "hello there"])))
  (with-instrumentation `re-find
    #?(:clj (caught? `re-find (re-find 1)))
    (is (caught? `re-find (re-find 1 "s")))
    (is (caught? `re-find (re-find #"s" 1)))))

(deftest subs-test
  (is (check-call `subs ["foo" 0 2]))
  (testing "start and end equal to count of s"
    (is (= "" (check-call `subs ["foo" 2 2]))))
  (with-instrumentation `subs
    (testing "not a string"
      (is (caught? `subs (subs nil 10)))
      (is (caught? `subs (subs 1 2 3))))
    (testing "not a nat-int?"
      (is (caught? `subs (subs "foo" "bar")))
      (is (caught? `subs (subs "foo" 0 "baz"))))
    (testing "start index too large"
      (is (caught? `subs (subs "foo" 10))))
    (testing "end index too large"
      (is (caught? `subs (subs "foo" 0 20))))
    (testing "end before start"
      (is (caught? `subs (subs "foo" 2 0))))))

(deftest interpose-test
  (is (check-call `interpose [0]))
  (is (check-call `interpose [0 [1 1 1]]))
  (check `interpose)
  (with-instrumentation `interpose
    (testing "wrong amount of args"
      (is (caught? `interpose (interpose))))
    (testing "non-coll arg"
      (is (caught? `interpose (interpose 0 0))))))

(deftest next-test
  (is (nil? (check-call `next [[]])))
  (is (nil? (check-call `next [[1]])))
  (is (some? (check-call `next [[1 2]])))
  (check `next)
  ;; CLJS cannot yet instrument `next`
  ;; See: https://dev.clojure.org/jira/browse/CLJS-3023
  #?(:clj
     (with-instrumentation `next
       (testing "wrong type"
         (is (caught? `next (next 1)))))))

(deftest rest-test
  (is (= () (check-call `rest [[]])))
  (is (= () (check-call `rest [[1]])))
  (is (= '(2) (check-call `rest [[1 2]])))
  (check `rest)
  (with-instrumentation `rest
    (testing "wrong type"
      (is (caught? `rest (rest 1))))))

(deftest last-test
  (is (nil? (check-call `last [[]])))
  (is (= 1 (check-call `last [[1]])))
  (check `last)
  (with-instrumentation `last
    (testing "wrong type"
      (is (caught? `last (last 1))))))

(deftest inc-dec-test
  (is (check-call `inc [0]))
  (is (check-call `dec [0]))
  (check `inc)
  (check `dec)
  (with-instrumentation `inc
    (testing "wrong type"
      (is (caught? `inc (apply inc ["f"])))))
  (with-instrumentation `dec
    (testing "wrong type"
      (is (caught? `dec (apply dec ["f"]))))))

(deftest into-test
  (is (check-call `into []))
  (is (check-call `into [[1] [2]]))
  (is (check-call `into [[1] (map inc) [2]]))
  (is (check-call `into [{:a 1} {:b 2}]))
  (is (check-call `into [{:a 1} [[:b 2]]]))
  (is (rt/successful?
       (rt/check `into {:gen {::ss/conjable #(s/gen ::ss/map)
                              ::ss/reducible-coll
                              #(s/gen (s/or :map+ ::ss/map+
                                            :maps+ (s/+ ::ss/map+)
                                            :pairs (s/+ ::ss/pair)))}}
                 {:num-tests 50})))
  (is (rt/successful?
       (rt/check `into {:gen {::ss/conjable #(gen/such-that (comp not map?)
                                                            (s/gen ::ss/conjable))}}
                 {:num-tests 50})))
  (with-instrumentation `into
    (is (caught? `into (into :a)))
    (is (caught? `into (into [] :a)))
    (is (caught? `into (into [] (map inc) :a)))))

(deftest group-by-test
  (is (check-call `group-by [odd? (range 10)]))
  (with-instrumentation `group-by
    (is (caught? `group-by (group-by 1 (range 10))))
    (is (caught? `group-by (group-by odd? 1)))))

(deftest conj-test
  (is (check-call `conj []))
  (is (nil? (check-call `conj [nil])))
  (is (check-call `conj [[]]))
  (is (check-call `conj [[] 1]))
  (is (check-call `conj [[] 1 2 3 4 5]))
  (is (rt/successful?
       (rt/check `conj {:gen {::ss/conjable #(s/gen ::ss/map)
                              ::ss/any #(s/gen (s/or :map+ ::ss/map+
                                                     :pair ::ss/pair))}}
                 {:num-tests 50})))
  (is (rt/successful?
       (rt/check `conj {:gen {::ss/conjable #(gen/such-that (comp not map?)
                                                            (s/gen ::ss/conjable))}}
                 {:num-tests 50})))
  (with-instrumentation `conj
    (is (caught? `conj (conj 1)))))

;;;; Scratch

(comment
  (t/run-tests)
  (stest/unstrument)
  )
