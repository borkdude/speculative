(ns speculative.specs
  "Primitive specs"
  (:refer-clojure :exclude [seqable? reduceable?])
  (:require [clojure.spec-alpha2 :as s]
            [clojure.spec-alpha2.gen :as gen]
            #?(:cljs [goog.string])))

#?(:cljs
   (if (and *clojurescript-version*
            (pos? (goog.string/compareVersions "1.10.439"
                                               *clojurescript-version*)))
     (defn seqable? [v]
       (or (nil? v)
           (clojure.core/seqable? v)))
     (def seqable? clojure.core/seqable?))
   :clj (def seqable? clojure.core/seqable?))

(defn reducible? [x]
  #?(:clj
     (instance? clojure.lang.IReduceInit x)
     :cljs (clojure.core/reduceable? x)))

#?(:clj
   (defn- iterable? [x]
     (instance? java.lang.Iterable x)))

(s/def ::associative associative?)
(s/def ::any any?)
(s/def ::boolean boolean?)
(s/def ::counted counted?)
(s/def ::ifn ifn?)
(s/def ::int int?)
(s/def ::nat-int nat-int?)
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

(s/def ::seqable-of-map-entry
  (s/coll-of ::map-entry :kind seqable?))

(s/def ::predicate ::ifn)

(s/def ::transducer ::ifn)

(s/def ::seqable-or-transducer
  (s/or :seqable ::seqable
        :transducer ::transducer))

(s/def ::atom
  (fn [a]
    #?(:clj (instance? clojure.lang.IAtom a)
       :cljs (satisfies? IAtom a))))

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument)
  )
