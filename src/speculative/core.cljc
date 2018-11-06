(ns speculative.core
  (:refer-clojure :exclude [map-entry? reduceable?])
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]))

(defn reducible? [x]
  #?(:clj
     (instance? clojure.lang.IReduceInit x)
     :cljs (clojure.core/reduceable? x)))

#?(:clj
   (defn- iterable? [x]
     (instance? java.lang.Iterable x)))

(defn map-entry? [v]
  #?(:cljs (clojure.core/map-entry? v)
     :clj #(instance? java.util.Map$Entry %)))

(s/def ::associative associative?)
(s/def ::any any?)
(s/def ::boolean boolean?)
(s/def ::counted counted?)
(s/def ::ifn ifn?)
(s/def ::int int?)
(s/def ::iterable iterable?)
(s/def ::map map?)
(s/def ::map-entry
  (s/with-gen map-entry?
    (fn []
      (gen/fmap first
                (s/gen (s/and ::map seq))))))
(s/def ::nil nil?)
(s/def ::number number?)
(s/def ::reducible reducible?)
(s/def ::seqable seqable?)
(s/def ::some some?)
(s/def ::string string?)

(s/fdef clojure.core/=
  :args (s/+ ::any)
  :ret ::boolean)

(s/fdef clojure.core//
  :args (s/cat :numerator ::number
               :denominators (s/* ::number))
  :ret ::number)

(s/fdef clojure.core/apply
  :args (s/cat :f ::ifn
               :intervening (s/* ::any)
               :args (s/nilable ::seqable)))

(s/fdef clojure.core/assoc
  :args (s/cat :map (s/nilable ::associative)
               :key ::any :val ::any :kvs (s/* (s/cat :ks ::any :vs ::any)))
  :ret ::associative)

(s/fdef clojure.core/count
  :args (s/cat :coll (s/or :counted ::counted :seqable ::seqable))
  :ret ::int)

(s/def ::predicate ::ifn)

(s/fdef clojure.core/every?
  :args (s/cat :pred ::predicate :coll (s/nilable ::seqable))
  :ret ::boolean)

(s/def ::transducer ::ifn)

(s/def ::seqable-or-transducer
  (s/or :seqable ::seqable
        :transducer ::transducer))

(s/fdef clojure.core/filter
  :args (s/alt :transducer (s/cat :xf ::ifn)
               :seqable (s/cat :f ::ifn :coll (s/nilable ::seqable)))
  :ret ::seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

(s/fdef clojure.core/first
  :args (s/cat :coll (s/nilable ::seqable)))

(s/fdef clojure.core/fnil
  :args (s/cat :f ::ifn :xs (s/+ ::any))
  :ret ::ifn)

(s/fdef clojure.core/get
  :args (s/cat :map ::any
               :key ::any
               :default (s/* ::any))
  :ret ::any)

(s/fdef clojure.core/juxt
  :args (s/+ ::ifn)
  :ret ::ifn)

(s/fdef clojure.core/map
  :args (s/alt :transducer (s/cat :xf ::ifn)
               :seqable (s/cat :f ::ifn :colls
                               (s/+ (s/nilable ::seqable))))
  :ret ::seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

(s/fdef clojure.core/merge
  :args (s/cat :maps (s/* (s/nilable ::associative)))
  :ret (s/nilable ::associative))

(s/fdef clojure.core/merge-with
  :args (s/cat :f ::ifn :maps (s/* (s/nilable ::associative)))
  :ret (s/nilable ::associative))

(s/fdef clojure.core/not-any?
  :args (s/cat :pred ::predicate :coll (s/nilable ::seqable))
  :ret ::boolean)

(s/fdef clojure.core/not-every?
  :args (s/cat :pred ::predicate :coll (s/nilable ::seqable))
  :ret ::boolean)

(s/fdef clojure.core/range
  :args (s/alt :infinite (s/cat)
               :finite (s/cat :start (s/? ::number)
                              :end ::number
                              :step (s/? ::number)))
  :ret ::seqable)

(s/fdef clojure.core/partial
  :args (s/cat :f ::ifn :args (s/* ::any))
  :ret ::ifn)

(s/def ::reducible-coll
  (s/nilable (s/or :reducible ::reducible
                   :iterable   ::iterable
                   :seqable    ::seqable)))

(s/fdef clojure.core/reduce
  :args (s/cat :f ::ifn :val (s/? ::any) :coll ::reducible-coll))

(s/fdef clojure.core/remove
  :args (s/cat :pred ::predicate
               :coll (s/? (s/nilable ::seqable)))
  :ret ::seqable-or-transducer)

(s/def ::atom
  (fn [a]
    #?(:clj (instance? clojure.lang.IAtom a)
       :cljs (satisfies? IAtom a))))

(s/fdef clojure.core/reset!
  :args (s/cat :atom ::atom :v ::any))

(s/fdef clojure.core/some
  :args (s/cat :pred ::predicate :coll (s/nilable ::seqable))
  :ret (s/or :found ::some :not-found ::nil))

(s/fdef clojure.core/some?
  :args (s/cat :x ::any)
  :ret ::boolean)

(s/fdef clojure.core/str
  :args (s/* ::any)
  :ret ::string)

(s/fdef clojure.core/swap!
  :args (s/cat :atom ::atom :f ::ifn :args (s/* ::any)))

(comment
  (stest/instrument)
  (stest/unstrument)
  )
