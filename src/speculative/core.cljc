(ns speculative.core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(s/fdef clojure.core/=
  :args (s/+ any?)
  :ret boolean?)

(def non-zero? (complement zero?))

#?(:clj (s/fdef clojure.core//
          :args (s/or :single-arity (s/cat :non-zero-number
                                           (s/and number? non-zero?))
                      :multi-arity (s/cat :number
                                          number?
                                          :non-zero-numbers
                                          (s/+ (s/and number? non-zero?))))
          :ret number?)
   :cljs (s/fdef clojure.core//
           :args (s/cat :numbers (s/+ number?))
           :ret number?))

(s/fdef clojure.core/apply
  :args (s/cat :f ifn?
               :intervening (s/* any?)
               :args (s/nilable seqable?)))

(s/fdef clojure.core/assoc
  :args (s/or :map (s/cat :map (s/nilable map?) :key any? :val any? :kvs (s/* (s/cat :ks any? :vs any?)))
              :vector (s/cat :map vector? :key nat-int? :val any? :kvs (s/* (s/cat :ks nat-int? :vs any?))))
  :ret associative?)

(s/fdef clojure.core/count
  :args (s/cat :coll (s/or :counted counted? :seqable seqable?))
  :ret number?)

(s/def ::predicate ifn?)

(s/fdef clojure.core/every?
  :args (s/cat :pred ::predicate :coll (s/nilable seqable?))
  :ret boolean?)

(s/def ::transducer fn?)

(s/def ::seq-or-transducer
  (s/or :seq seq? :transducer ::transducer))

(s/fdef clojure.core/filter
  :args (s/cat :pred ::predicate :coll (s/? (s/nilable seqable?)))
  :ret ::seq-or-transducer)

(s/fdef clojure.core/first
  :args (s/cat :coll (s/nilable seqable?)))

(s/fdef clojure.core/fnil
  :args (s/cat :f ifn? :xs (s/+ any?))
  :ret ifn?)

(s/fdef clojure.core/juxt
  :args (s/+ ifn?)
  :ret fn?)

(s/fdef clojure.core/map
  :args (s/cat :f ifn? :colls (s/* (s/nilable seqable?)))
  :ret ::seq-or-transducer)

(s/fdef clojure.core/merge
  :args (s/cat :maps (s/* (s/nilable map?)))
  :ret (s/nilable map?))

(s/fdef clojure.core/merge-with
  :args (s/cat :f ifn? :maps (s/* (s/nilable map?)))
  :ret (s/nilable map?))

(s/fdef clojure.core/not-any?
  :args (s/cat :pred ::predicate :coll (s/nilable seqable?))
  :ret boolean?)

(s/fdef clojure.core/not-every?
  :args (s/cat :pred ::predicate :coll (s/nilable seqable?))
  :ret boolean?)

(s/fdef clojure.core/range
  :args (s/nilable
         (s/and (s/coll-of int?)
                #(<= (count %) 3)))
  :ret seq?)

#?(:clj
   (defn- reduceable? [x]
     (or (instance? clojure.lang.IReduce x)
         (instance? clojure.lang.IReduceInit x))))

#?(:clj
   (defn- iterable? [x]
     (instance? java.lang.Iterable x)))

(s/def ::reduceable-coll (s/nilable (s/or :reduceable reduceable?
                                          :iterable   iterable?
                                          :seqable    seqable?)))

(s/fdef clojure.core/reduce
  :args (s/cat :f ifn? :val (s/? any?) :coll ::reduceable-coll))

(s/fdef clojure.core/remove
  :args (s/cat :pred ::predicate :coll (s/? (s/nilable seqable?)))
  :ret ::seq-or-transducer)

(s/def ::atom
  (fn [a]
    #?(:clj (instance? clojure.lang.IAtom a)
       :cljs (satisfies? IAtom a))))

(s/fdef clojure.core/reset!
  :args (s/cat :atom ::atom :v any?))

(s/fdef clojure.core/some
  :args (s/cat :pred ::predicate :coll (s/nilable seqable?))
  :ret (s/or :found some? :not-found nil))

(s/fdef clojure.core/some?
  :args (s/cat :x any?)
  :ret boolean?)

(s/fdef clojure.core/str
  :args (s/* any?)
  :ret string?)

(s/fdef clojure.core/swap!
  :args (s/cat :atom ::atom :f ifn? :args (s/* any?)))

(comment
  (stest/instrument)
  (stest/unstrument)
  )
