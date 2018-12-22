(ns speculative.specs
  "Primitive specs"
  (:refer-clojure :exclude [seqable? reduceable? regexp?])
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
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
(s/def ::set set?)
(s/def ::nil nil?)
(s/def ::number number?)
(s/def ::reducible reducible?)
(s/def ::seqable seqable?)
(s/def ::sequential sequential?)
(s/def ::some some?)
(s/def ::string string?)
#?(:clj (s/def ::char-sequence
          (s/with-gen
            #(instance? java.lang.CharSequence %)
            (fn []
              (gen/one-of (map #(gen/fmap %
                                          (s/gen ::string))
                               [#(StringBuffer. %)
                                #(StringBuilder. %)
                                #(java.nio.CharBuffer/wrap %)
                                #(String. %)]))))))

(s/def ::seqable-of-map-entry
  (s/coll-of ::map-entry :kind seqable?))

(s/def ::seqable-of-string
  (s/coll-of ::string :kind seqable?))

(s/def ::string-or-seqable-of-string
  (s/or :string ::string
        :seqable ::seqable-of-string))

(s/def ::reducible-coll
  (s/or
   :seqable    ::seqable
   :reducible  (s/nilable ::reducible)
   :iterable   (s/nilable ::iterable)))

(s/def ::coll coll?)

(s/def ::predicate ::ifn)

(s/def ::transducer ::ifn)

(s/def ::seqable-or-transducer
  (s/or :seqable ::seqable
        :transducer ::transducer))

(s/def ::atom
  (fn [a]
    #?(:clj (instance? clojure.lang.IAtom a)
       :cljs (satisfies? IAtom a))))

#?(:clj
   (defn regexp? [r]
     (instance? java.util.regex.Pattern r))
   :cljs (def regexp? cljs.core/regexp?))

(s/def ::regexp
  (s/with-gen
    regexp?
    (fn []
      (gen/fmap re-pattern
                (s/gen ::string)))))

#?(:clj
   (s/def ::matcher
     #(instance? java.util.regex.Matcher %)))

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument)
  )
