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
   [speculative.test-utils :refer [gentest]]))

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

#?(:clj
   (deftest apply-test
     (is (= 21 (check `apply [+ 1 2 3 [4 5 6]])))
     (is (= 0 (check `apply [+ nil])))
     (with-instrumentation `apply
       (throws `apply (apply + 1 2 3 4))))
   :cljs nil
   ;; waiting for https://dev.clojure.org/jira/browse/CLJS-2942
   )

#?(:clj
   (deftest assoc-test
     (is (check `assoc [nil 'lol 'lol]))
     (is (check `assoc [{} 'lol 'lol 'bar 'lol]))
     (is (check `assoc [[] 0 'lol]))
     (with-instrumentation `assoc
       (throws `assoc (assoc 'lol 'lol 'lol))
       (throws `assoc (assoc {} 'lol))))
   :cljs nil
   ;; bug in planks implementation of spec
   ;;    ClojureScript 1.10.339
   ;; cljs.user=> (ns speculative.core
   ;;        #_=>   (:require [clojure.spec.alpha :as s]
   ;;        #_=>             [clojure.spec.test.alpha :as stest]))
   ;; nil
   ;; speculative.core=> (s/fdef clojure.core/assoc
   ;;               #_=>   :args (s/cat :map (s/nilable associative?) :key any? :val any? :kvs (s/* (s/cat :ks any? :vs any?)))
   ;;                                                                                                                                        #_=              #_=>   :ret map?)
   ;; cljs.core/assoc
   ;; speculative.core=>   (stest/instrument)
   ;;    speculative.core=>   (stest/instrument)
   ;; Vector's key for assoc must be a number.
   ;;   cljs.core/-assoc [cljs.core/IAssociative] (cljs/core.cljs:5546:14)
   ;;   cljs.core/-assoc (cljs/core.cljs:630:24)
   ;;   cljs.core/apply-to (cljs/core.cljs:3845:1)
   ;;   cljs.spec.test.alpha/c (cljs/spec/test/alpha.cljs:120:34)
   ;;   cljs.spec.test.alpha/d (cljs/spec/test/alpha.cljs:114:29)
   ;;   cljs.core/-assoc [cljs.core/IAssociative] (cljs/core.cljs:6621:6)
   ;;   cljs.core/-assoc (cljs/core.cljs:630:24)
   ;;   cljs.core/apply-to (cljs/core.cljs:3845:1)
   ;;   cljs.spec.test.alpha/c (cljs/spec/test/alpha.cljs:120:34)
   ;;   cljs.spec.test.alpha/d (cljs/spec/test/alpha.cljs:114:29)
   ;;   cljs/lang/applyTo (cljs/core.cljs:1961:7)
   ;;   cljs.spec.test.alpha/f (cljs/spec/test/alpha.cljs:113:16)
   ;;   cljs.spec.alpha/rep* (cljs/spec/alpha.cljs:953:10)
   ;;   cljs.spec.alpha/deriv (cljs/spec/alpha.cljs:1090:22)
   ;;   cljs.spec.alpha/deriv (cljs/spec/alpha.cljs:1087:50)
   ;;   cljs.spec.alpha/re-conform (cljs/spec/alpha.cljs:1211:17)
   ;;   cljs.spec.alpha/conform* [cljs.spec.alpha/Spec] (cljs/spec/alpha.cljs:1252:10)
   ;;   cljs.spec.alpha/conform* (cljs/spec/alpha.cljs:39:1)
   ;;   cljs.spec.alpha/conform (cljs/spec/alpha.cljs:153:4)
   ;;   cljs.spec.test.alpha/f (cljs/spec/test/alpha.cljs:110:39)
   ;;   planck.pprint.width-adjust/generate-sample (planck/pprint/width_adjust.cljs:5:218)
   ;;   planck.pprint.width-adjust/bisect (planck/pprint/width_adjust.cljs:7:302)
   ;;   planck.pprint.width-adjust/adjusted-with (planck/pprint/width_adjust.cljs:11:143)
   ;;   planck.pprint.width-adjust/d (planck/pprint/width_adjust.cljs:12:301)
   ;;   planck.repl/print-value (planck/repl.cljs:1940:12)
   ;;   cljs.core/e (cljs/core.cljs:4240:17)
   ;;   cljs.js/B (cljs/js.cljs:1133:24)
   ;;   cljs.js/eval-str* (cljs/js.cljs:1047:6)
   ;;   planck.repl/process-execute-source (planck/repl.cljs:2003:11)
   ;;   planck.repl/execute-source (planck/repl.cljs:2056:18)
   ;;   planck.repl/execute (planck/repl.cljs:2065:7)
   )

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

#?(:clj
   (deftest merge-test
     (is (check `merge [{}]))
     (is (nil? (check `merge [])))
     (is (check `merge [{} nil]))
     (is (nil? (check `merge [nil])))
     (with-instrumentation `merge
       (throws `merge (merge 1))))
   :cljs
   ;; waiting for https://dev.clojure.org/jira/browse/CLJS-2942
   nil)
#?(:clj
   (deftest merge-with-test
     (is (check `merge-with [+ {}]))
     (is (nil? (check `merge-with [+])))
     (is (check `merge-with [+ {} nil]))
     (is (nil? (check `merge-with [+ nil])))
     (with-instrumentation `merge-with
       (throws `merge-with (merge-with 1))))
   :cljs
   ;; waiting for https://dev.clojure.org/jira/browse/CLJS-2942
   nil)

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
  (is (check `range [1]))
  (is (check `range [1 10]))
  (is (check `range [10 0 -1]))
  (is (check `range [1.1 2.2 3.3]))
  (with-instrumentation `range
    ;; https://dev.clojure.org/jira/browse/CLJS-2948
    ;; (is (range))
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
