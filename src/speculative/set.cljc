(ns speculative.set
  (:require
   [clojure.set :as set]
   #?(:clj [clojure.spec-alpha2 :as s]
      :cljs [clojure.spec.alpha :as s])
   [speculative.specs :as ss]))

(s/def ::nilable-set
  (s/nilable ::ss/set))

(s/def ::nilable-map
  (s/nilable ::ss/map))

(s/def ::rel
  (s/nilable (s/coll-of ::nilable-map)))

(s/def ::rel*
  (s/nilable (s/coll-of ::ss/seqable-of-map-entry)))

(s/def ::seqable-of-pairs
  (s/coll-of (s/or :map-entry ::ss/map-entry
                   :pair (s/coll-of ::ss/any :count 2))
             :kind seqable?))

(s/def ::nullary
  (s/cat))

(s/def ::unary
  (s/cat :s1 ::nilable-set))

(s/def ::binary
  (s/cat :s1 ::nilable-set
         :s2 ::nilable-set))

(s/def ::variadic
  (s/cat :s1 ::nilable-set
         :s2 ::nilable-set
         :sets (s/* ::nilable-set)))

(s/fdef set/union
  :args (s/alt :nullary ::nullary
               :unary ::unary
               :binary ::binary
               :variadic ::variadic)
  :ret ::nilable-set)

(s/fdef set/intersection
  :args (s/alt :unary ::unary
               :binary ::binary
               :variadic ::variadic)
  :ret ::nilable-set)

(s/fdef set/difference
  :args (s/alt :unary ::unary
               :binary ::binary
               :variadic ::variadic)
  :ret ::nilable-set)

(s/fdef set/select
  :args (s/cat :pred ::ss/predicate
               :xset ::nilable-set)
  :ret ::nilable-set)

(s/fdef set/project
  :args (s/cat :xrel ::rel*
               :ks ::ss/sequential)
  :ret ::ss/set)

(s/fdef set/rename-keys
  :args (s/cat :map ::nilable-map
               :kmap ::nilable-map)
  :ret ::nilable-map)

(s/fdef set/rename
  :args (s/cat :xrel ::rel
               :kmap ::nilable-map)
  :ret ::ss/set)

(s/fdef set/index
  :args (s/cat :xrel ::rel*
               :ks ::ss/sequential)
  :ret ::ss/map)

(s/fdef set/map-invert
  :args (s/cat :m (s/nilable ::seqable-of-pairs))
  :ret ::ss/map)

(s/fdef set/join
  :args (s/alt :binary (s/cat :xrel ::rel
                              :yrel ::rel*)
               :ternary (s/cat :xrel ::rel
                               :yrel ::rel*
                               :km ::nilable-map))
  :ret ::ss/set)

(s/fdef set/subset?
  :args (s/cat :set1 ::nilable-set
               :set2 ::nilable-set)
  :ret ::ss/boolean)

(s/fdef set/superset?
  :args (s/cat :set1 ::nilable-set
               :set2 ::nilable-set)
  :ret ::ss/boolean)
