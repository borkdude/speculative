(ns speculative.set-test
  (:require
   [clojure.set :as set]
   [clojure.test :as t :refer [are deftest is]]
   [respeced.test :refer [with-instrumentation
                          with-unstrumentation
                          caught?
                          check-call]]
   [speculative.set]
   [speculative.test-utils :refer [check]]))

(deftest union-test
  (is (= #{1 2 3} (check-call `set/union [#{1 2} #{3}])))
  (is (= #{1 2} (check-call `set/union [#{1 2} nil])))
  (is (= nil (check-call `set/union [nil nil])))
  (check `set/union)
  (with-instrumentation `set/union
    (is (caught? `set/union (set/union #{1 2} [3])))
    (is (caught? `set/union (set/union [1 2] #{3})))
    (is (caught? `set/union (set/union {1 2} #{3})))))

(deftest intersection-test
  (is (= #{1} (check-call `set/intersection [#{1 2} #{1}])))
  (is (= nil (check-call `set/intersection [nil #{1}])))
  (is (= nil (check-call `set/intersection [nil nil])))
  (check `set/intersection)
  (with-instrumentation `set/intersection
    (is (caught? `set/intersection (set/intersection #{1 2} [1])))
    (is (caught? `set/intersection (set/intersection [1 2] #{1})))
    (is (caught? `set/intersection (set/intersection {1 2} #{1})))))

(deftest difference-test
  (is (= #{2} (check-call `set/difference [#{1 2} #{1}])))
  (is (= nil (check-call `set/difference [nil #{1}])))
  (is (= #{1 2} (check-call `set/difference [#{1 2} nil])))
  (is (= nil (check-call `set/difference [nil nil])))
  (check `set/difference)
  (with-instrumentation `set/difference
    (is (caught? `set/difference (set/difference #{1 2} [1])))
    (is (caught? `set/difference (set/difference [1 2] #{1})))
    (is (caught? `set/difference (set/difference {1 2} [1])))))

(deftest select-test
  (is (= #{1 2} (check-call `set/select [int? #{1 2 :a}])))
  (is (= nil (check-call `set/select [int? nil])))
  (check `set/select)
  (with-instrumentation `set/select
    (is (caught? `set/select (set/select int? [1 2 :a])))
    (is (caught? `set/select (set/select int? {:a 1})))))

(deftest project-test
  (is (= #{{:a 1}} (check-call `set/project [[{:a 1 :b 2}] [:a]])))
  (is (= #{{:a 1}} (check-call `set/project [[{:a 1 :b 2}] '(:a)])))
  #?(:clj (is (= #{{:a 1}} (check-call `set/project [#{(java.util.HashMap. {:a 1 :b 2})} [:a]]))))
  (is (= #{{:a 1}} (check-call `set/project [#{{:a 1 :b 2}} [:a]])))
  (is (= #{} (check-call `set/project [nil [:a]])))
  (is (= #{{}} (check-call `set/project [#{nil} [:a]])))
  (check `set/project)
  (with-instrumentation `set/project
    (is (caught? `set/project (set/project nil nil)))
    (is (caught? `set/project (set/project #{[:a 1 :b 2]} {:a nil})))
    (is (caught? `set/project (set/project {:a 1 :b 2} [:a])))))

(deftest rename-keys-test
  (is (= {:c 1 :d 2} (check-call `set/rename-keys [{:a 1 :b 2} {:a :c :b :d}])))
  (is (= nil (check-call `set/rename-keys [nil {:a :c :b :d}])))
  (is (= {:a 1 :b 2} (check-call `set/rename-keys [{:a 1 :b 2} nil])))
  (with-instrumentation `set/rename-keys
    (is (caught? `set/rename-keys (set/rename-keys [:a 1 :b 2] {:a :c :b :d})))
    (is (caught? `set/rename-keys (set/rename-keys {:a 1 :b 2} [:a :c :b :d])))
    #?(:clj (is (caught? `set/rename-keys (set/rename-keys (java.util.HashMap. {:a 1 :b 2}) {:a :c :b :d}))))))

(deftest rename-test
  (is (= #{{:b 1 :c 1} {:b 2 :c 2}} (check-call `set/rename [#{{:a 1 :b 1} {:a 2 :b 2}} {:a :c}])))
  (is (= #{{:b 1 :c 1} {:b 2 :c 2}} (check-call `set/rename [[{:a 1 :b 1} {:a 2 :b 2}] {:a :c}])))
  (is (= #{} (check-call `set/rename [nil {:a :c}])))
  (is (= #{nil} (check-call `set/rename [[nil] {:a :c}])))
  (is (= #{} (check-call `set/rename [nil nil])))
  (is (= #{nil} (check-call `set/rename [[nil] nil])))
  (with-instrumentation `set/rename
    (is (caught? `set/rename (set/rename [[:a 1 :b 1]] {:a :c})))
    (is (caught? `set/rename (set/rename [{:a 1 :b 1}] [:a :c])))
    #?(:clj (is (caught? `set/rename (set/rename [(java.util.HashMap. {:a 1 :b 1})] [:a :c]))))))

(deftest index-test
  (is (= {{:a 1} #{{:a 1 :b 2}}} (check-call `set/index [#{{:a 1 :b 2}} [:a]])))
  (is (= {{:a 1} #{{:a 1 :b 2}}} (check-call `set/index [#{{:a 1 :b 2}} '(:a)])))
  (is (= {{:a 1} #{{:a 1 :b 2}}} (check-call `set/index [[{:a 1 :b 2}] [:a]])))
  #?(:clj (is (= {{:a 1} #{(java.util.HashMap. {:a 1 :b 2})}}
                 (check-call `set/index [#{(java.util.HashMap. {:a 1 :b 2})} [:a]]))))
  (is (= {} (check-call `set/index [nil [:a]])))
  (is (= {{} #{nil}} (check-call `set/index [[nil] [:a]])))
  ;; TODO: fix
  (check `set/index)
  (with-instrumentation `set/index
    (is (caught? `set/index (set/index [[:a 1 :b 2]] [:a])))
    (is (caught? `set/index (set/index [{:a 1 :b 2}] :a)))))

(deftest map-invert-test
  (is (= {:b :a} (check-call `set/map-invert [{:a :b}])))
  (is (= {1 :a 2 :b} (check-call `set/map-invert ['([:a 1] [:b 2])])))
  (is (= {1 :a 2 :b} (check-call `set/map-invert [[[:a 1] [:b 2]]])))
  (is (= {} (check-call `set/map-invert [nil])))
  #?(:clj (is (= {1 :a 2 :b} (check-call `set/map-invert [(java.util.HashMap. {:a 1 :b 2})]))))
  (check `set/map-invert)
  (with-instrumentation `set/map-invert
    (is (caught? `set/map-invert (set/map-invert [[:a :b :c]])))
    (is (caught? `set/map-invert (set/map-invert [:a :b])))
    (is (caught? `set/map-invert (set/map-invert #{:a :b})))))

(deftest join-test
  (are [x y] (= x y)
    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [#{{:a 1} {:a 2}} #{{:a 1 :b 1} {:a 2 :b 2}}])

    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [#{{:a 1} {:a 2}} [{:a 1 :b 1} {:a 2 :b 2}]])

    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [[{:a 1} {:a 2}] #{{:a 1 :b 1} {:a 2 :b 2}}])

    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [[{:a 1} {:a 2}] [{:a 1 :b 1} {:a 2 :b 2}]])

    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [#{{:a 1} {:a 2}} #{{:b 1} {:b 2}} {:a :b}])

    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [#{{:a 1} {:a 2}} [{:b 1} {:b 2}] {:a :b}])

    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [[{:a 1} {:a 2}] #{{:b 1} {:b 2}} {:a :b}])

    #{{:a 1 :b 1} {:a 2 :b 2}}
    (check-call `set/join [[{:a 1} {:a 2}] [{:b 1} {:b 2}] {:a :b}])

    #{}
    (check-call `set/join [nil [{:b 1} {:b 2}] {:a :b}])

    #{}
    (check-call `set/join [[{:a 1} {:a 2}] nil {:a :b}])

    #{}
    (check-call `set/join [[{:a 1} {:a 2}] nil nil])

    #{nil}
    (check-call `set/join [[nil] [nil] nil]))
  (check `set/join)
  (with-instrumentation `set/join
    (is (caught? `set/join (set/join [{:a 1}] [{:b 2}] [:a :b])))
    (is (caught? `set/join (set/join [[:a 1]] [{:b 2}] {:a :b})))
    (is (caught? `set/join (set/join [{:a 1}] [[:b 2]] {:a :b})))
    #?(:clj (is (caught? `set/join (set/join [(java.util.HashMap. {:a 1})] [{:b 2}] {:a :b}))))))

(deftest subset?-test
  (is (true? (check-call `set/subset? [#{:a} #{:a :b}])))
  (is (true? (check-call `set/subset? [nil #{:a :b}])))
  (is (false? (check-call `set/subset? [#{:a} nil])))
  (check `set/subset?)
  (with-instrumentation `set/subset?
    (is (caught? `set/subset? (set/subset? #{:a} [:a :b])))
    (is (caught? `set/subset? (set/subset? [:a] #{:a :b})))
    (is (caught? `set/subset? (set/subset? [:a] [:a :b])))))

(deftest superset?-test
  (is (true? (check-call `set/superset? [#{:a :b} #{:a}])))
  (is (false? (check-call `set/superset? [nil #{:a}])))
  (is (true? (check-call `set/superset? [#{:a :b} nil])))
  (check `set/superset?)
  (with-instrumentation `set/superset?
    (is (caught? `set/superset? (set/superset? [:a :b] #{:a})))
    (is (caught? `set/superset? (set/superset? #{:a :b} [:a])))
    (is (caught? `set/superset? (set/superset? [:a :b] [:a])))))

;;;; Scratch

(comment
  (t/run-tests *ns*)
  )
