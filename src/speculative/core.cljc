(ns speculative.core
  "Specs for clojure.core"
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [speculative.specs :as ss]))

;; fdefs sorted in order of appearance in
;; https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj

;; 16
(s/fdef clojure.core/list
  :args (s/cat :items (s/* ::ss/any))
  :ret ::ss/list)

;; 22
(s/fdef clojure.core/cons
  :args (s/cat :x ::ss/any :seq ::ss/seqable)
  :ret ::ss/non-empty-seq)

;; 49
(s/fdef clojure.core/first
  :args (s/cat :coll ::ss/seqable)
  :ret ::ss/any)

;; 57
(s/fdef clojure.core/next
  :args (s/cat :coll ::ss/seqable)
  :ret ::ss/seqable)

;; 66
(s/fdef clojure.core/rest
  :args (s/cat :coll ::ss/seqable)
  :ret ::ss/seqable)

;; 75
(s/fdef clojure.core/conj
  :args (s/alt :no-args (s/cat)
               :args (s/cat :coll ::ss/conjable :xs (s/* ::ss/any)))
  :ret ::ss/conjable)

;; 126
(s/fdef clojure.core/seq
  :args (s/cat :coll ::ss/seqable)
  :ret (s/nilable ::ss/seq))

;; 181 assoc
;; defined separately to make overridable generator
(s/def ::assoc-args
  (s/cat :map (s/nilable ::ss/associative)
         :key ::ss/any :val ::ss/any :kvs (s/* (s/cat :ks ::ss/any :vs ::ss/any))))

(s/fdef clojure.core/assoc
  :args ::assoc-args
  :ret ::ss/associative)

;; 262
(s/fdef clojure.core/last
  :args (s/cat :coll ::ss/seqable)
  :ret ::ss/any)

;; 353
(s/fdef clojure.core/vector
  :args (s/* ::ss/any)
  :ret ::ss/vector)

;; 367
(s/fdef clojure.core/vec
  :args (s/cat :coll ::ss/reducible-coll)
  :ret ::ss/vector)

;; 379
(s/fdef clojure.core/hash-map
  :args (s/cat :pairs (s/* (s/cat :k ::ss/any :v ::ss/any)))
  :ret ::ss/map)

;; 389
(s/fdef clojure.core/hash-set
  :args (s/cat :keys (s/* ::ss/any))
  :ret ::ss/set)

;; 436
(s/fdef clojure.core/nil?
  :args (s/cat :x ::ss/any)
  :ret ::ss/boolean)

;; 524
(s/fdef clojure.core/not
  :args (s/cat :x ::ss/any)
  :ret ::ss/boolean)

;; 531
(s/fdef clojure.core/some?
  :args (s/cat :x ::ss/any)
  :ret ::ss/boolean)

;; 544
(s/fdef clojure.core/str
  :args (s/* ::ss/any)
  :ret ::ss/string)

;; 648
(s/fdef clojure.core/list*
  :args (s/cat :intervening (s/* ::ss/any)
               :args ::ss/seqable)
  :ret ::ss/seqable)

;; 660
(s/fdef clojure.core/apply
  :args (s/cat :f ::ss/ifn
               :intervening (s/* ::ss/any)
               :args ::ss/seqable)
  :ret ::ss/any)

;; 718
(s/fdef clojure.core/concat
  :args (s/* ::ss/seqable)
  :ret ::ss/seqable)

;; 783
(s/fdef clojure.core/=
  :args (s/+ ::ss/any)
  :ret ::ss/boolean)

;; 874
(s/fdef clojure.core/count
  :args (s/cat :coll (s/or :counted ::ss/counted :seqable ::ss/seqable))
  :ret ::ss/int)

;; 889
(s/def ::nth-args
  (s/cat :coll ::ss/nthable
         :index ::ss/int
         :not-found (s/? ::ss/any)))

(s/fdef clojure.core/nth
  :args ::nth-args
  :ret ::ss/any)

;; 922
(s/fdef clojure.core/inc
  :args (s/cat :x ::ss/number)
  :ret ::ss/number)

;; 984
(s/fdef clojure.core/+
  :args (s/* ::ss/number)
  :ret ::ss/number)

;; 1008
(s/fdef clojure.core/*
  :args (s/* ::ss/number)
  :ret ::ss/number)

;; 1020
(s/fdef clojure.core//
  :args (s/cat :numerator ::ss/number
               :denominators (s/* ::ss/number))
  :ret ::ss/number)

;; 1043
(s/fdef clojure.core/-
  :args (s/+ ::ss/number)
  :ret ::ss/number)

;; 1142
(s/fdef clojure.core/dec
  :args (s/cat :x ::ss/number)
  :ret ::ss/number)

;; 1115
(s/fdef clojure.core/max
  :args (s/+ ::ss/number)
  :ret ::ss/number)

;; 1125
(s/fdef clojure.core/min
  :args (s/+ ::ss/number)
  :ret ::ss/number)

;; 1247
(s/fdef clojure.core/pos?
  :args (s/cat :num ::ss/number)
  :ret ::ss/boolean)

;; 1254
(s/fdef clojure.core/neg?
  :args (s/cat :num ::ss/number)
  :ret ::ss/boolean)

(s/fdef clojure.core/even?
  :args (s/cat :n int?)
  :ret boolean?)

;; 1459
(s/fdef clojure.core/peek
  :args (s/cat :coll (s/nilable ::ss/stack))
  :ret ::ss/any)

;; 1467
(s/fdef clojure.core/pop
  :args (s/cat :coll (s/nilable ::ss/stack))
  :ret (s/nilable ::ss/stack))

;; 1494
(s/fdef clojure.core/get
  :args (s/cat :map ::ss/any
               :key ::ss/any
               :default (s/? ::ss/any))
  :ret ::ss/any)

;; 1504
(s/fdef clojure.core/dissoc
  :args (s/cat :map (s/nilable ::ss/map) :keys (s/* ::ss/any))
  :ret (s/nilable ::ss/map))

;; 1534
(s/fdef clojure.core/find
  :args (s/cat :map
               (s/nilable (s/or :associative ::ss/associative
                                #?@(:clj [:java-map ::ss/java-map])))
               :key ::ss/any)
  :ret (s/nilable ::ss/map-entry))

;; 1540
(s/fdef clojure.core/select-keys
  :args (s/cat :map
               (s/nilable (s/or :associative ::ss/associative
                                #?@(:clj [:java-map ::ss/java-map])))
               :keyseq ::ss/seqable)
  :ret ::ss/map)

;; 1555
(s/fdef clojure.core/keys
  :args (s/cat :map ::ss/seqable-of-map-entry)
  :ret ::ss/seqable)

;; 1561
(s/fdef clojure.core/vals
  :args (s/cat :map ::ss/seqable-of-map-entry)
  :ret ::ss/seqable)

;; 1589
(s/fdef clojure.core/name
  :args (s/cat :x (s/or :string ::ss/string
                        :named ::ss/named))
  :ret ::ss/string)

;; 2327
(s/fdef clojure.core/atom
  :args (s/cat :x ::ss/any
               :options
               ::ss/atom.options)
  :ret ::ss/atom)

;; 2345
(s/fdef clojure.core/swap!
  :args (s/cat :atom ::ss/atom :f ::ss/ifn :args (s/* ::ss/any)))

;; 2376
(s/fdef clojure.core/reset!
  :args (s/cat :atom ::ss/atom :v ::ss/any)
  :ret ::ss/any)

;; 2557
(s/fdef clojure.core/comp
  :args (s/* ::ss/ifn)
  :ret ::ss/ifn)

;; 2576
(s/fdef clojure.core/juxt
  :args (s/+ ::ss/ifn)
  :ret ::ss/ifn)

;; 2672
(s/fdef clojure.core/every?
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret ::ss/boolean)

;; 2684
(s/fdef clojure.core/not-every?
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret ::ss/boolean)

;; 2614
(s/fdef clojure.core/partial
  :args (s/cat :f ::ss/ifn :args (s/* ::ss/any))
  :ret ::ss/ifn)

;; 2692
(s/fdef clojure.core/some
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret (s/or :found ::ss/some :not-found ::ss/nil))

;; 2703
(s/fdef clojure.core/not-any?
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret ::ss/boolean)

;; 2727
(s/fdef clojure.core/map
  :args (s/alt :transducer (s/cat :f ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :colls (s/+ ::ss/seqable)))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 2793
(s/fdef clojure.core/filter
  :args (s/alt :transducer (s/cat :f ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 2826
(s/fdef clojure.core/remove
  :args (s/alt :transducer (s/cat :f ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 3019
(s/fdef clojure.core/range
  :args (s/alt :infinite (s/cat)
               :finite (s/cat :start (s/? ::ss/number)
                              :end ::ss/number
                              :step (s/? ::ss/number)))
  :ret ::ss/seqable)

;; 3041
(s/fdef clojure.core/merge
  :args (s/cat :maps (s/? (s/cat
                           :init-map (s/nilable ::ss/map)
                           :rest-maps (s/* ::ss/seqable-of-map-entry))))
  :ret (s/nilable map?))

;; 3051
(s/fdef clojure.core/merge-with
  :args (s/cat :f ::ss/ifn
               :maps (s/? (s/cat
                           :init-map (s/nilable map?)
                           :rest-maps (s/* ::ss/seqable-of-map-entry))))
  :ret (s/nilable map?))

;; 3071
(s/fdef clojure.core/zipmap
  :args (s/cat :keys ::ss/seqable
               :vals ::ss/seqable)
  :ret ::ss/map)

;; 3184
(s/fdef clojure.core/partition
  :args (s/alt :no-overlap (s/cat :n ::ss/int :coll ::ss/seqable)
               :step (s/cat :n ::ss/int :step ::ss/int :coll ::ss/seqable)
               :pad (s/cat :n ::ss/int :step ::ss/int
                           :pad ::ss/seqable :coll ::ss/seqable))
  :ret ::ss/seqable)

;; 4839
(s/fdef clojure.core/re-pattern
  :args (s/alt :re ::ss/regex :s ::ss/string)
  :ret ::ss/regex)

;; 4849
#?(:clj
   (s/fdef clojure.core/re-matcher
     :args ::ss/regex+string-args
     :ret ::ss/matcher))

;; 4858
#?(:clj
   (s/fdef clojure.core/re-groups
     :args (s/cat :m ::ss/matcher)
     :ret ::ss/regex-match))

;; 4874
(s/fdef clojure.core/re-seq
  :args ::ss/regex+string-args
  :ret ::ss/regex-matches)

;; 4886
(s/fdef clojure.core/re-matches
  :args ::ss/regex+string-args
  :ret ::ss/regex-match)

;; 4898
(s/fdef clojure.core/re-find
  :args #?(:clj (s/alt :matcher (s/cat :m ::ss/matcher)
                       :re-s ::ss/regex+string-args)
           :cljs ::ss/regex+string-args)
  :ret ::ss/regex-match)

;; 4981
(s/def ::subs-args
  (s/and (s/cat :s ::ss/string
                :start ::ss/nat-int
                :end (s/? ::ss/nat-int))
         (fn start-idx [{:keys [s start end]}]
           (let [end (or end (count s))]
             (<= start end (count s))))))

(s/fdef clojure.core/subs
  :args ::subs-args
  :ret ::ss/string)

;; 4989
(s/fdef clojure.core/max-key
  :args (s/cat :k ::ss/ifn :xs (s/+ ::ss/any))
  :ret ::ss/any)

;; 5009
(s/fdef clojure.core/min-key
  :args (s/cat :k ::ss/ifn :xs (s/+ ::ss/any))
  :ret ::ss/any)

;; 5029
(s/fdef clojure.core/distinct
  :args (s/alt :transducer (s/cat)
               :seqable (s/cat :coll ::ss/reducible-coll))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 5206
(s/fdef clojure.core/interpose
  :args (s/alt :transducer (s/cat :sep ::ss/any)
               :seqable (s/cat :sep ::ss/any :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 6142
(s/fdef clojure.core/get-in
  :args (s/cat :map (s/nilable ::ss/associative)
               :keys ::ss/seqable
               :not-found (s/? ::ss/any))
  :ret ::ss/any)

;; 6152
;; defined separately to make overridable generator
(s/def ::assoc-in-args
  (s/cat :map (s/nilable ::ss/associative)
         :keys ::ss/non-empty-seqable
         :val ::ss/any))

(s/fdef clojure.core/assoc-in
  :args ::assoc-in-args
  :ret ::ss/associative)

;; 6172
(s/def ::update-in-args
  (s/cat :map (s/nilable ::ss/associative)
         :keys ::ss/non-empty-seqable
         :f ::ss/ifn
         :args (s/* ::ss/any)))

(s/fdef clojure.core/update-in
  :args ::update-in-args
  :ret ::ss/associative)

;; 6188
(s/def ::update-args
  (s/cat :map (s/nilable ::ss/associative)
         :key ::ss/any
         :f ::ss/ifn
         :args (s/* ::ss/any)))

(s/fdef clojure.core/update
  :args ::update-args
  :ret ::ss/associative)

;; 6536
(s/fdef clojure.core/fnil
  :args (s/cat :f ::ss/ifn :x ::ss/any :y (s/? ::ss/any) :z (s/? ::ss/any))
  :ret ::ss/ifn)

;; 6790
(s/fdef clojure.core/reduce
  :args (s/cat :f ::ss/ifn :val (s/? ::ss/any) :coll ::ss/reducible-coll))

;; 6887
(s/fdef clojure.core/into
  :args (s/alt :no-arg (s/cat)
               :identity (s/cat :to ::ss/conjable)
               :seqable (s/cat :to ::ss/conjable :from ::ss/reducible-coll)
               :transducer (s/cat :to ::ss/conjable :xf ::ss/transducer :from ::ss/reducible-coll))
  :ret ::ss/seqable)

;; 7136
(s/fdef clojure.core/flatten
  :args (s/cat :x (s/nilable ::ss/sequential))
  :ret ::ss/sequential-of-non-sequential)

;; 7146
(s/fdef clojure.core/group-by
  :args (s/cat :f ::ss/ifn :coll ::ss/reducible-coll)
  :ret ::ss/map
  :fn (fn [{:keys [args ret]}]
        (let [[_ coll] (:coll args)]
          (= (count coll)
             (reduce + (map count (vals ret)))))))

;; 7160
(s/fdef clojure.core/partition-by
  :args (s/alt :transducer (s/cat :f ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer)

;; 7203
(s/fdef clojure.core/frequencies
  :args (s/cat :coll ::ss/reducible-coll)
  :ret (s/map-of ::ss/any nat-int?))

;; 7240
(s/fdef clojure.core/partition-all
  :args (s/alt :transducer (s/cat :n ::ss/int)
               :no-overlap (s/cat :n ::ss/int :coll ::ss/seqable)
               :step (s/cat :n ::ss/int :step ::ss/int :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer)

;; 7274
(s/fdef clojure.core/shuffle
  :args (s/cat :coll
               ;; See https://dev.clojure.org/jira/browse/CLJ-2470
               #?(:clj ::ss/java-coll
                  :cljs (s/nilable
                         (s/or :coll ::ss/coll
                               :array ::ss/array))))
  :ret ::ss/coll)

;; 7283
(s/fdef clojure.core/map-indexed
  :args (s/alt :transducer (s/cat :f ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))


;; 7313
(s/fdef clojure.core/keep
  :args (s/alt :transducer (s/cat :f ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 7346
(s/fdef clojure.core/keep-indexed
  :args (s/alt :transducer (s/cat :f ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 7396
(s/fdef clojure.core/every-pred
  :args (s/cat :pred (s/+ ::ss/predicate))
  :ret ::ss/ifn)

;; 7436
(s/fdef clojure.core/some-fn
  :args (s/cat :pred (s/+ ::ss/ifn))
  :ret ::ss/ifn)

;; 7655
(s/fdef clojure.core/dedupe
  :args (s/alt :transducer (s/cat)
               :seqable (s/cat :coll ::ss/reducible-coll))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument))

;;;;
