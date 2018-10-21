(ns speculative.core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(s/def ::transducer fn?)

(s/def ::seq-or-transducer
  (s/or :seq seq? :transducer ::transducer))

(s/fdef clojure.core/map
  :args (s/cat :f ifn? :colls (s/* (s/nilable seqable?)))
  :ret ::seq-or-transducer)

(s/fdef clojure.core/merge
  :args (s/coll-of (s/nilable map?))
  :ret (s/nilable map?))

(s/fdef clojure.core/filter
  :args (s/cat :pred ifn? :coll (s/? (s/nilable seqable?)))
  :ret ::seq-or-transducer)

(s/fdef clojure.core/merge-with
  :args (s/cat :f ifn? :maps (s/+ (s/nilable map?)))
  :ret (s/nilable map?))

(s/fdef clojure.core/fnil
  :args (s/cat :f ifn? :xs (s/+ any?))
  :ret ifn?)

(s/fdef clojure.core/=
  :args (s/+ any?)
  :ret boolean?)

(s/fdef clojure.core/str
  :args (s/* any?)
  :ret string?)

;; This doesn't work all that well because it seems like
;; we use generative testing to verify that a higher order fn
;; satisfies the spec

(s/def ::reducing-fn (s/fspec :args (s/cat :accumulator any? :elem any?)))

;; (s/valid? ::reducing-fn (fn [x y]) ;; takes forever

#_(s/fdef clojure.core/reduce
  :args (s/cat :f ::reducing-fn :val any? :coll sequential?))

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
  :args (s/or :binary  (s/cat :f fn? :coll ::reduceable-coll)
              :trinary (s/cat :f fn? :val any? :coll ::reduceable-coll)))

(comment
  (stest/instrument)
  )
