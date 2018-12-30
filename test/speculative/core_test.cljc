(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as t :refer [is deftest testing]]
   [clojure.set :as set]
   [speculative.specs :as ss]
   [speculative.core]
   [respeced.test :as rt :refer [with-instrumentation
                                 with-unstrumentation
                                 caught?
                                 check-call]]
   [speculative.test-utils :refer [check planck-env?]]
   ;; included for self-hosted cljs
   [workarounds-1-10-439.core]))

;; sorted in order of appearance in
;; https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj

;; 49
(deftest first-test
  (is (nil? (check-call `first [nil])))
  (is (= 1 (check-call `first ['(1 2 3)])))
  (with-instrumentation `first
    (is (caught? `first (first 1)))))

;; 57
(deftest next-test
  (is (nil? (check-call `next [[]])))
  (is (nil? (check-call `next [[1]])))
  (is (some? (check-call `next [[1 2]])))
  ;; check commented, because this puts next in an instrumented state in self-hosted
  (check `next)
  ;; CLJS cannot yet instrument `next`
  ;; See: https://dev.clojure.org/jira/browse/CLJS-3023
  #?(:clj
     (with-instrumentation `next
       (testing "wrong type"
         (is (caught? `next (next 1)))))))

;; 66
(deftest rest-test
  (is (= () (check-call `rest [[]])))
  (is (= () (check-call `rest [[1]])))
  (is (= '(2) (check-call `rest [[1 2]])))
  (check `rest)
  (with-instrumentation `rest
    (testing "wrong type"
      (is (caught? `rest (rest 1))))))

;; 75
(deftest conj-test
  (is (check-call `conj []))
  (is (nil? (check-call `conj [nil])))
  (is (check-call `conj [[]]))
  (is (check-call `conj [[] 1]))
  (is (check-call `conj [[] 1 2 3 4 5]))
  (check `conj {:gen {::ss/conjable #(s/gen ::ss/map)
                      ::ss/any #(s/gen (s/or :map+ ::ss/map+
                                             :pair ::ss/pair))}})
  (check `conj {:gen {::ss/conjable #(gen/such-that (comp not map?)
                                                    (s/gen ::ss/conjable))}})
  (with-instrumentation `conj
    (is (caught? `conj (conj 1)))))

;; 181
(deftest assoc-test
  (is (check-call `assoc [nil 'lol 'lol]))
  (is (check-call `assoc [{} 'lol 'lol 'bar 'lol]))
  (is (check-call `assoc [[] 0 'lol]))
  (check `assoc)
  (with-instrumentation `assoc
    (is (caught? `assoc (assoc 'lol 'lol 'lol)))
    (is (caught? `assoc (assoc {} 'lol)))))

;; 262
(deftest last-test
  (is (nil? (check-call `last [[]])))
  (is (= 1 (check-call `last [[1]])))
  (check `last)
  (with-instrumentation `last
    (testing "wrong type"
      (is (caught? `last (last 1))))))

;; 531
(deftest some?-test
  (is (check-call `some? [1]))
  (check `some?))

;; 544
(deftest str-test
  (is (= "" (check-call `str [nil])))
  (is (= "lolfoo" (check-call `str ["lol" "foo"])))
  (check `str)
  ;; there's really no way to make str crash, is there?
  (with-instrumentation `str))

;; 660
(deftest apply-test
  (is (= 21 (check-call `apply [+ 1 2 3 [4 5 6]])))
  (is (= 0 (check-call `apply [+ nil])))
  #?(:clj (with-instrumentation `apply
            (is (caught? `apply (apply + 1 2 3 4))))
     :cljs nil)) ;; maximum call stack exceeded


;; 783
(deftest =-test
  (is (check-call `= [1]))
  (is (check-call `= [1 1]))
  (check `=)
  (with-instrumentation `=
    (is (caught? `= (=)))))

;; 874
(deftest count-test
  (is (check-call `count [nil]))
  (is (check-call `count [[1]]))
  (is (check-call `count [{:a 1}]))
  (is (check-call `count [(into-array [1 2])]))
  (is (check-call `count ["abc"]))
  (with-instrumentation `count
    (is (caught? `count (apply count [1])))))

;; 922 inc, 1142 dec
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

;; 984
(deftest plus-minus-times-test
  (check `+)
  #?(:clj
     (let [times *]
       (with-redefs [* (fn [& args]
                         (try (apply times args)
                              (catch ArithmeticException _ 0)))]
         (check `*)))
     :cljs (check `*))
  (check `-)
  (with-instrumentation `+
    (is (caught? `+ (apply + ["f"]))))
  (with-instrumentation `*
    (is (caught? `* (apply * ["f"]))))
  (with-instrumentation `-
    (is (caught? `- (apply - []))))
  (with-instrumentation `-
    (is (caught? `- (apply - ["f"])))))

;; 1020
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

;; 1115 - 1125 min/max
(deftest min-max-test
  (is (check-call `min [1]))
  (is (check-call `max [1]))
  (is (check-call `min [1 2]))
  (is (check-call `max [1 2]))
  (check `min)
  (check `max)
  (with-instrumentation `min
    (is (caught? `min (apply min ["0" "1"]))))
  (with-instrumentation `max
    (is (caught? `max (apply max ["0" "1"])))))

;; 1494
(deftest get-test
  (is (= 'foo (check-call `get [#{'foo} 'foo 'bar])))
  (is (nil? (check-call `get [1 1])))
  (with-instrumentation `get
    (is (caught? `get (get)))))

;; 1540
(deftest select-keys-test
  (is (check-call `select-keys [nil nil]))
  (is (check-call `select-keys [{:a 1} [:a]]))
  (check `select-keys)
  (with-instrumentation `select-keys
    (is (caught? `select-keys (select-keys 1 [])))
    (is (caught? `select-keys (select-keys {} 1)))))

;; 2345
(deftest swap!-test
  (is (nil? (check-call `swap! [(atom nil) identity])))
  (is (nil? (check-call `swap! [(atom nil) (fn [x y]) 1])))
  (with-instrumentation `swap!
    (is (caught? `swap! (swap! 1 identity)))
    (is (caught? `swap! (swap! (atom nil) 1) (+ 1 2 3)))))

;; 2376
(deftest reset!-test
  (is (check-call `reset! [(atom nil) 1]))
  (with-instrumentation `reset!
    (is (caught? `reset! (reset! 1 (atom nil))))))

;; 2576
(deftest juxt-text
  (is (= [1 2] ((check-call `juxt [:a :b]) {:a 1 :b 2})))
  (with-instrumentation `juxt
    (is (caught? `juxt (juxt 1 2 3)))))

;; 2672
(deftest every?-test
  (is (check-call `every? [pos? nil]))
  (is (check-call `every? [identity nil]))
  (with-instrumentation `every?
    (is (caught? `every? (every? 1 [])))))

;; 2684
(deftest not-every?-test
  (is (false? (check-call `not-every? [pos? nil])))
  (is (check-call `not-every? [pos? [-1 1]]))
  (with-instrumentation `not-every?
    (is (caught? `not-every? (not-every? 1 [])))))

;; 2614
(deftest partial-test
  (is (check-call `partial [identity]))
  (is (check-call `partial [+ 1 2 3]))
  (with-instrumentation `partial
    (is (caught? `partial (partial 1)))))

;; 2692
(deftest some-test
  (is (not (check-call `some [pos? nil])))
  (is (nil? (check-call `some [identity nil])))
  (with-instrumentation `some
    (is (caught? `some (some 1 [])))))

;; 2703
(deftest not-any?-test
  (is (check-call `not-any? [pos? nil]))
  (is (= true (check-call `not-any? [identity nil])))
  (with-instrumentation `not-any?
    (is (caught? `not-any? (not-any? 1 [])))))

;; 2727
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

;; 2793
(deftest filter-test
  (is (check-call `filter [pos?]))
  (is (check-call `filter [pos? nil]))
  (is (= '()  (check-call `filter [identity nil])))
  (with-instrumentation `filter
    (is (caught? `filter (filter 1)))))

;; 2826
(deftest remove-test
  (is (check-call `remove [pos?]))
  (is (check-call `remove [pos? nil]))
  (is (= '() (check-call `remove [identity nil])))
  (with-instrumentation `remove
    (is (caught? `remove (remove 1)))))

;; 3019
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

;; 3041
(deftest merge-test
  (is (check-call `merge [{}]))
  (is (nil? (check-call `merge [])))
  (is (check-call `merge [{} nil]))
  (is (nil? (check-call `merge [nil])))
  #?(:clj (is (= {:a 1 :b 2}
                 (merge {:a 1} (java.util.HashMap. {:a 1 :b 2})))))
  (with-instrumentation `merge
    (is (caught? `merge (merge 1)))))

;; 3051
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
  ;;(println "were instrumented:" (stest/unstrument))
  (with-instrumentation `merge-with
    (is (caught? `merge-with (merge-with 1)))
    ;; the following is no longer allowed in CLJS, see CLJS-2943
    (is (caught? `merge-with (merge-with + {:a 1} [[:a 2]])))))

;; 4839
(deftest re-pattern-test
  (is (check-call `re-pattern ["s"]))
  (check `re-pattern)
  (with-instrumentation `re-pattern
    (is (caught? `re-pattern (re-pattern 1)))))

;; 4849
#?(:clj
   (deftest re-matcher-test
     (is (check-call `re-matcher [#"s" "s"]))
     (with-instrumentation `re-matcher
       (is (caught? `re-matcher (re-matcher 1 "s")))
       (is (caught? `re-matcher (re-matcher #"s" 1))))
     (check `re-matcher)))

;; 4858
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

;; 4874
(deftest re-seq-test
  (testing "no matches"
    (is (nil? (check-call `re-seq [#"a" "b"]))))
  (testing "one match"
    (is (check-call `re-seq [#"s" "s"])))
  (with-instrumentation `re-seq
    (is (caught? `re-seq (re-seq 1 "s")))
    (is (caught? `re-seq (re-seq #"s" 1))))
  (check `re-seq))

;; 4886
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

;; 4898
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

;; 4981
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

;; 5206
(deftest interpose-test
  (is (check-call `interpose [0]))
  (is (check-call `interpose [0 [1 1 1]]))
  (check `interpose)
  (with-instrumentation `interpose
    (testing "wrong amount of args"
      (is (caught? `interpose (interpose))))
    (testing "non-coll arg"
      (is (caught? `interpose (interpose 0 0))))))

;; 6152
(deftest assoc-in-test
  (is (check-call `assoc-in [[] [0] :val]))
  (is (check-call `assoc-in [[] '(0) :val]))
  (is (check-call `assoc-in [{} [:a] :val]))
  (is (check-call `assoc-in [nil [:a] :val]))
  (check `assoc-in)
  (with-instrumentation `assoc-in
                        (testing "first arg not an associative/nil"
                          (is (caught? `assoc-in (assoc-in '() [0] :val))))
                        (testing "Provided ks not a sequential"
                          (is (caught? `assoc-in (assoc-in [] 0 :val)))))
  (testing "Index out of bounds" (is (thrown? #?(:clj java.lang.IndexOutOfBoundsException
                                                 :cljs js/Error)
                                              (check-call `assoc-in [[] [1] :val])))))

;; 6536
(deftest fnil-test
  (is (check-call `fnil [identity 'lol]))
  (with-instrumentation `fnil
    (is (caught? `fnil (fnil 1 1)))))

;; 6790
(deftest reduce-test
  (is (check-call `reduce [+ [1 2]]))
  (is (check-call `reduce [+ 0 [1 2]]))
  (with-instrumentation `reduce
    (is (caught? `reduce (reduce 1 [1 2])))
    (is (caught? `reduce (reduce + 0 1)))))

;; 6887
(deftest into-test
  (is (check-call `into []))
  (is (check-call `into [[1] [2]]))
  (is (check-call `into [[1] (map inc) [2]]))
  (is (check-call `into [{:a 1} {:b 2}]))
  (is (check-call `into [{:a 1} [[:b 2]]]))
  (check `into {:gen {::ss/conjable #(s/gen ::ss/map)
                      ::ss/reducible-coll
                      #(s/gen (s/or :map+ ::ss/map+
                                    :maps+ (s/+ ::ss/map+)
                                    :pairs (s/+ ::ss/pair)))}})
  (check `into {:gen {::ss/conjable #(gen/such-that (comp not map?)
                                                    (s/gen ::ss/conjable))}})
  (with-instrumentation `into
    (is (caught? `into (into :a)))
    (is (caught? `into (into [] :a)))
    (is (caught? `into (into [] (map inc) :a)))))

;; 7136
(deftest flatten-test
  (is (check-call `flatten [nil]))
  (is (check-call `flatten [[]]))
  (is (check-call `flatten [[:a]]))
  (is (check-call `flatten [[[:a :b]]]))
  (check `flatten)
  (with-instrumentation
    `flatten
    (is (caught? `flatten (flatten :not-a-sequential)))
    (is (caught? `flatten (flatten #{:still :not :a :sequential})))))

;; 7146
(deftest group-by-test
  (is (check-call `group-by [odd? (range 10)]))
  (with-instrumentation `group-by
    (is (caught? `group-by (group-by 1 (range 10))))
    (is (caught? `group-by (group-by odd? 1)))))

;;;; Scratch

(comment
  (t/run-tests)
  (stest/unstrument))
