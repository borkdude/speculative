(ns speculative.core
  "Specs for clojure.core"
  (:require
   [clojure.spec.alpha :as s]
   [speculative.specs :as ss]))

;; fdefs sorted in order of appearance in
;; https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj

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

;; 262
(s/fdef clojure.core/last
  :args (s/cat :coll ::ss/seqable)
  :ret ::ss/any)

;; 531
(s/fdef clojure.core/some?
  :args (s/cat :x ::ss/any)
  :ret ::ss/any)

;; 544
(s/fdef clojure.core/str
  :args (s/* ::ss/any)
  :ret ::ss/string)

;; 660
(s/fdef clojure.core/apply
  :args (s/cat :f ::ss/ifn
               :intervening (s/* ::ss/any)
               :args ::ss/seqable))

;; 783
(s/fdef clojure.core/=
  :args (s/+ ::ss/any)
  :ret ::ss/boolean)

;; 1494
(s/fdef clojure.core/get
  :args (s/cat :map ::ss/any
               :key ::ss/any
               :default (s/? ::ss/any))
  :ret ::ss/any)

;; 181
(s/fdef clojure.core/assoc
  :args (s/cat :map (s/nilable ::ss/associative)
               :key ::ss/any :val ::ss/any :kvs (s/* (s/cat :ks ::ss/any :vs ::ss/any)))
  :ret ::ss/associative)

;; 874
(s/fdef clojure.core/count
  :args (s/cat :coll (s/or :counted ::ss/counted :seqable ::ss/seqable))
  :ret ::ss/int)

;; 922
(s/fdef clojure.core/inc
  :args (s/cat :x ::ss/number)
  :ret ::ss/number)

;; 1020
(s/fdef clojure.core//
  :args (s/cat :numerator ::ss/number
               :denominators (s/* ::ss/number))
  :ret ::ss/number)

;; 1142
(s/fdef clojure.core/dec
  :args (s/cat :x ::ss/number)
  :ret ::ss/number)

;; 2345
(s/fdef clojure.core/swap!
  :args (s/cat :atom ::ss/atom :f ::ss/ifn :args (s/* ::ss/any)))

;; 2376
(s/fdef clojure.core/reset!
  :args (s/cat :atom ::ss/atom :v ::ss/any))

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
  :args (s/alt :transducer (s/cat :xf ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :colls
                               (s/+ ::ss/seqable)))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 2793
(s/fdef clojure.core/filter
  :args (s/alt :transducer (s/cat :xf ::ss/ifn)
               :seqable (s/cat :f ::ss/ifn :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 2826
(s/fdef clojure.core/remove
  :args (s/cat :pred ::ss/predicate
               :coll (s/? ::ss/seqable))
  :ret ::ss/seqable-or-transducer)

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
                           :init-map (s/nilable map?)
                           :rest-maps (s/* ::ss/seqable-of-map-entry))))
  :ret (s/nilable map?))

;; 3051
(s/fdef clojure.core/merge-with
  :args (s/cat :f ::ss/ifn
               :maps (s/? (s/cat
                           :init-map (s/nilable map?)
                           :rest-maps (s/* ::ss/seqable-of-map-entry))))
  :ret (s/nilable map?))

;; 4839
(s/fdef clojure.core/re-pattern
  :args (s/cat :s ::ss/string)
  :ret ::ss/regexp)

;; 4849
#?(:clj
   (s/fdef clojure.core/re-matcher
     :args (s/cat :re ::ss/regexp :s ::ss/string)
     :ret ::ss/matcher))

;; 4858
#?(:clj
   (s/fdef clojure.core/re-groups
     :args (s/cat :m ::ss/matcher)
     :ret ::ss/string-or-seqable-of-string))

;; 4874
(s/fdef clojure.core/re-seq
  :args (s/cat :re ::ss/regexp :s ::ss/string)
  :ret ::ss/seqable-of-string)

;; 4886
(s/fdef clojure.core/re-matches
  :args (s/cat :re ::ss/regexp :s ::ss/string)
  :ret ::ss/string-or-seqable-of-string)

;; 4898
(s/fdef clojure.core/re-find
  :args #?(:clj (s/alt :matcher (s/cat :m ::ss/matcher)
                       :re-s (s/cat :re ::ss/regexp :s ::ss/string))
           :cljs (s/cat :re ::ss/regexp :s ::ss/string))
  :ret ::ss/string-or-seqable-of-string)

;; 4981
(s/fdef clojure.core/subs
  :args (s/and (s/cat :s ::ss/string
                      :start ::ss/nat-int
                      :end (s/? ::ss/nat-int))
               (fn start-idx [{:keys [s start end]}]
                 (let [end (or end (count s))]
                   (<= start end (count s)))))
  :ret ::ss/string)

;; 5206
(s/fdef clojure.core/interpose
  :args (s/alt :transducer (s/cat :sep ::ss/any)
               :seqable (s/cat :sep ::ss/any :coll ::ss/seqable))
  :ret ::ss/seqable-or-transducer
  :fn (fn [{:keys [args ret]}]
        (= (key args) (key ret))))

;; 6536
(s/fdef clojure.core/fnil
  :args (s/cat :f ::ss/ifn :xs (s/+ ::ss/any))
  :ret ::ss/ifn)

;; 6790
(s/fdef clojure.core/reduce
  :args (s/cat :f ::ss/ifn :val (s/? ::ss/any) :coll ::ss/reducible-coll))

;; 7146
(s/fdef clojure.core/group-by
  :args (s/cat :f ::ss/ifn :coll ::ss/reducible-coll)
  :ret map?
  :fn (fn [{:keys [args ret]}]
        (let [[_ coll] (:coll args)]
          (= (count coll)
             (reduce + (map count (vals ret)))))))

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument)
  )
