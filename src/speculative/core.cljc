(ns speculative.core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(s/fdef clojure.core/=
  :args (s/+ any?)
  :ret boolean?)

(s/fdef clojure.core//
  :args (s/cat :numerator number?
               :denominators (s/* number?))
  :ret number?)

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
  :ret int?)

(s/def ::predicate ifn?)

(s/fdef clojure.core/every?
  :args (s/cat :pred ::predicate :coll (s/nilable seqable?))
  :ret boolean?)

(s/def ::transducer ifn?)

(s/def ::seqable-or-transducer
  (s/or :seqable seqable?
        :transducer ::transducer))

(s/fdef clojure.core/filter
  :args (s/alt :transducer (s/cat :xf ifn?)
               :seqable (s/cat :f ifn? :coll (s/nilable seqable?)))
  :ret ::seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

(s/fdef clojure.core/first
  :args (s/cat :coll (s/nilable seqable?)))

(s/fdef clojure.core/fnil
  :args (s/cat :f ifn? :xs (s/+ any?))
  :ret ifn?)

(s/fdef clojure.core/get
  :args (s/cat :map any?
               :key any?
               :default (s/* any?))
  :ret any?)

(s/fdef clojure.core/juxt
  :args (s/+ ifn?)
  :ret ifn?)

(s/fdef clojure.core/map
  :args (s/alt :transducer (s/cat :xf ifn?)
               :seqable (s/cat :f ifn? :colls
                               (s/+ (s/nilable seqable?))))
  :ret ::seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

(s/fdef clojure.core/merge
  :args (s/cat :maps (s/* (s/nilable associative?)))
  :ret (s/nilable associative?))

(s/fdef clojure.core/merge-with
  :args (s/cat :f ifn? :maps (s/* (s/nilable associative?)))
  :ret (s/nilable associative?))

(s/fdef clojure.core/not-any?
  :args (s/cat :pred ::predicate :coll (s/nilable seqable?))
  :ret boolean?)

(s/fdef clojure.core/not-every?
  :args (s/cat :pred ::predicate :coll (s/nilable seqable?))
  :ret boolean?)

(s/fdef clojure.core/range
  :args (s/alt :infinite (s/cat)
               :finite (s/cat :start (s/? number?)
                              :end number?
                              :step (s/? number?)))
  :ret seqable?)

(s/fdef clojure.core/partial
  :args (s/cat :f ifn? :args (s/* any?))
  :ret ifn?)

#?(:clj
   (defn- reduceable? [x]
     (or (instance? clojure.lang.IReduce x)
         (instance? clojure.lang.IReduceInit x))))

#?(:clj
   (defn- iterable? [x]
     (instance? java.lang.Iterable x)))

(s/def ::reduceable-coll
  (s/nilable (s/or :reduceable reduceable?
                   :iterable   iterable?
                   :seqable    seqable?)))

(s/fdef clojure.core/reduce
  :args (s/cat :f ifn? :val (s/? any?) :coll ::reduceable-coll))

(s/fdef clojure.core/remove
  :args (s/cat :pred ::predicate
               :coll (s/? (s/nilable seqable?)))
  :ret ::seqable-or-transducer)

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
