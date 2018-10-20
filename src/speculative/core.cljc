(ns speculative.core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(s/fdef clojure.core/map
  :args (s/cat :f ifn? :colls (s/? (s/nilable seqable?)))
  :ret seq?)

(s/fdef clojure.core/merge
  :args (s/coll-of (s/nilable map?))
  :ret (s/nilable map?))

;; This doesn't work all that well because it seems like
;; we use generative testing to verify that a higher order fn
;; satisfies the spec

(s/def ::reducing-fn (s/fspec :args (s/cat :accumulator any? :elem any?)))

;; (s/valid? ::reducing-fn (fn [x y]) ;; takes forever

#_(s/fdef clojure.core/reduce
  :args (s/cat :f ::reducing-fn :val any? :coll sequential?))

(s/fdef clojure.core/reduce
  :args (s/cat :f fn? :val any? :coll sequential?))

(comment
  (stest/instrument `clojure.core/map)
  (stest/instrument `clojure.core/reduce))
