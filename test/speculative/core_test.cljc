(ns speculative.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [speculative.core :as speculative] :reload
   [speculative.test-utils :refer [with-instrumentation
                                   throws
                                   check]]))

(deftest test-utils-test
  (testing "Sanity check for test infrastructure"
    (defn foo [n]
      "ret")
    (s/fdef foo
      :args (s/cat :n number?)
      :ret number?)
    (with-instrumentation `foo
      (is (throws `foo
                  "Specification-based check failed"
                  (check `foo [1]))))))

(deftest =-test
  (with-instrumentation `=
    (is (= 1))
    (is (= 1 1))
    (throws `= (=))))

(deftest division-test
  ;; Note: / is inlined when used with more than one argument
  ;; apply is one way to work around this
  (with-instrumentation `/
    (is (apply / [1]))
    (is (apply / [1 2]))
    (is (apply / [1 2 3]))
    (testing "Divide by zero, no spec error"
      #?(:cljs (is (= ##Inf (/ 0)))
         :clj (is (thrown-with-msg? java.lang.ArithmeticException
                                    #"Divide by zero"
                                    (/ 0)))))
    (throws `/ (apply / nil))
    (throws `/ (apply / ['a]))
    ))

#?(:clj
   (deftest apply-test
     (with-instrumentation `apply
       (is (apply + 1 2 3 [4 5 6]))
       (is (apply + nil))
       (throws `apply (apply + 1 2 3 4))))
   :cljs nil
   ;; waiting for https://dev.clojure.org/jira/browse/CLJS-2942
   )

#?(:clj
   (deftest assoc-test
     (with-instrumentation `assoc
       (is (check `assoc [nil 'lol 'lol]))
       (is (check `assoc [{} 'lol 'lol 'bar 'lol]))
       (is (check `assoc [[] 0 'lol]))
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
  (with-instrumentation `count
    (is (check `count [nil]))
    (is (check `count [[1]]))
    (is (check `count [{:a 1}]))
    (is (check `count [(into-array [1 2])]))
    (is (check `count ["abc"]))
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

(deftest get-test
  (with-instrumentation `get
    (is (= 'foo (get #{'foo} 'foo 'bar)))
    (is (nil? (get 1 1)))))

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

#?(:clj
   (deftest merge-test
     (with-instrumentation `merge
       (is (merge {}))
       (is (nil? (merge)))
       (is (merge {} nil))
       (is (nil? (merge nil)))
       (throws `merge (merge 1))))
   :cljs
   ;; waiting for https://dev.clojure.org/jira/browse/CLJS-2942
   nil)
#?(:clj
   (deftest merge-with-test
     (with-instrumentation `merge-with
       (is (merge-with + {}))
       (is (nil? (merge-with +)))
       (is (merge-with + {} nil))
       (is (nil? (merge-with + nil)))
       (throws `merge-with (merge-with 1))))
   :cljs
   ;; waiting for https://dev.clojure.org/jira/browse/CLJS-2942
   nil)

(deftest not-any-test
  (with-instrumentation `not-any?
    (is (not-any? pos? nil))
    (is (= true  (not-any? identity nil)))
    (throws `not-any? (not-any? 1 []))))

(deftest not-every-test
  (with-instrumentation `not-every?
    (is (false? (not-every? pos? nil)))
    (is (not-every? pos? [-1 1]))
    (throws `not-every? (not-every? 1 []))))

(deftest range-test
  (with-instrumentation `range
    (is (range))
    (is (range 1))
    (is (range 1 10))
    (is (range 10 0 -1))
    (is (range 1.1 2.2 3.3))
    (throws `range (range 'lol))
    (throws `range (range 0 1 2 3))))

(deftest partial-test
  (with-instrumentation `partial
    (is (partial identity))
    (is (partial + 1 2 3))
    (throws `partial (partial 1))))

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
    (throws `swap! (swap! (atom nil) 1) (+ 1 2 3))
    (is (nil? (swap! (atom nil) identity)))
    (is (nil? (swap! (atom nil) (fn [x y]) 1)))))

;;;; Scratch

(comment
  (t/run-tests)
  )
