(ns speculative.core
  "Specs for clojure.core"
  (:require
   [clojure.spec.alpha :as s]
   [speculative.specs :as ss]))

#?(:clj
   (s/fdef clojure.core/apply
     :args (s/cat :f ::ss/ifn
                  :intervening (s/* ::ss/any)
                  :args ::ss/seqable))
   ;; apply doesn't work on cljs
   :cljs nil)

(s/fdef clojure.core/assoc
  :args (s/cat :map (s/nilable ::ss/associative)
               :key ::ss/any :val ::ss/any :kvs (s/* (s/cat :ks ::ss/any :vs ::ss/any)))
  :ret ::ss/associative)

(s/fdef clojure.core/count
  :args (s/cat :coll (s/or :counted ::ss/counted :seqable ::ss/seqable))
  :ret ::ss/int)

(s/fdef clojure.core/every?
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret ::ss/boolean)

(s/fdef clojure.core/filter
  :args (s/alt :transducer (s/cat :xf ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

(s/fdef clojure.core/first
  :args (s/cat :coll ::ss/seqable))

(s/fdef clojure.core/fnil
  :args (s/cat :f ::ss/ifn :xs (s/+ ::ss/any))
  :ret ::ss/ifn)

(s/fdef clojure.core/juxt
  :args (s/+ ::ss/ifn)
  :ret ::ss/ifn)

(s/fdef clojure.core/map
  :args (s/alt :transducer (s/cat :xf ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :colls
                               (s/+ ::ss/seqable)))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

(s/fdef clojure.core/merge
  :args (s/cat :maps (s/* (s/nilable ::ss/associative)))
  :ret (s/nilable ::ss/associative))

(s/fdef clojure.core/merge-with
  :args (s/cat :f ::ss/ifn
               :maps (s/? (s/cat
                           :init-map (s/nilable map?)
                           :rest-maps (s/* ::ss/seqable-of-map-entry))))
  :ret (s/nilable map?))

(s/fdef clojure.core/not-any?
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret ::ss/boolean)

(s/fdef clojure.core/not-every?
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret ::ss/boolean)

(s/fdef clojure.core/range
  :args (s/alt :infinite (s/cat)
               :finite (s/cat :start (s/? ::ss/number)
                              :end ::ss/number
                              :step (s/? ::ss/number)))
  :ret ::ss/seqable)

(s/fdef clojure.core/partial
  :args (s/cat :f ::ss/ifn :args (s/* ::ss/any))
  :ret ::ss/ifn)

(s/def ::ss/reducible-coll
  (s/or
   :seqable    ::ss/seqable
   :reducible  (s/nilable ::ss/reducible)
   :iterable   (s/nilable ::ss/iterable)))

(s/fdef clojure.core/reduce
  :args (s/cat :f ::ss/ifn :val (s/? ::ss/any) :coll ::ss/reducible-coll))

(s/fdef clojure.core/remove
  :args (s/cat :pred ::ss/predicate
               :coll (s/? ::ss/seqable))
  :ret ::ss/seqable-or-transducer)

(s/fdef clojure.core/reset!
  :args (s/cat :atom ::ss/atom :v ::ss/any))

(s/fdef clojure.core/some
  :args (s/cat :pred ::ss/predicate :coll ::ss/seqable)
  :ret (s/or :found ::ss/some :not-found ::ss/nil))

(s/fdef clojure.core/swap!
  :args (s/cat :atom ::ss/atom :f ::ss/ifn :args (s/* ::ss/any)))

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument)
  )
