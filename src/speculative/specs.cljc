(ns speculative.specs
  "Primitive specs"
  (:refer-clojure :exclude [seqable? reduceable? regexp?])
  (:require #?(:clj [clojure.spec-alpha2 :as s]
               :cljs [clojure.spec.alpha :as s])
            #?(:clj [clojure.spec-alpha2.test :as stest]
               :cljs [clojure.spec.test.alpha :as stest])
            #?(:clj [clojure.spec-alpha2.gen :as gen]
               :cljs [clojure.spec.gen.alpha :as gen])
            #?(:cljs [goog.string])))

#?(:cljs (def before-1_10_439?
           (and *clojurescript-version*
                (pos? (goog.string/compareVersions "1.10.439"
                                                   *clojurescript-version*)))))

#?(:cljs
   (if before-1_10_439?
     (defn seqable? [v]
       (or (nil? v)
           (clojure.core/seqable? v)))
     (def seqable? clojure.core/seqable?))
   :clj (def seqable? clojure.core/seqable?))

(s/def ::seqable
  (s/with-gen seqable?
    (fn [] (s/gen clojure.core/seqable?))))

(defn reducible? [x]
  #?(:clj
     (instance? clojure.lang.IReduceInit x)
     :cljs (clojure.core/reduceable? x)))

#?(:clj
   (defn- iterable? [x]
     (instance? java.lang.Iterable x)))

(s/def ::associative associative?)

;; workaround for https://dev.clojure.org/jira/browse/CLJ-1966
(s/def ::any
  (s/with-gen
    (s/conformer #(if (s/invalid? %) ::invalid %))
    #(s/gen any?)))

(s/def ::boolean boolean?)
(s/def ::counted counted?)
(s/def ::ifn ifn?)
(s/def ::int int?)
(s/def ::nat-int nat-int?)
(s/def ::pos-int pos-int?)
(s/def ::iterable iterable?)
(s/def ::map map?)

(s/def ::java-map ::any)
;; FIXME: this doesn't work in spec-alpha2
#_#?(:clj (s/def ::java-map
          (s/with-gen #(instance? java.util.Map %)
            (fn [] (gen/fmap #(java.util.HashMap. %)
                             (s/gen ::map))))))
(s/def ::map+ #?(:cljs ::map :clj (s/or :map ::map :java-map ::java-map)))
(s/def ::map-entry
  (s/with-gen map-entry?
    (fn []
      (gen/fmap first
                (s/gen (s/and ::map seq))))))
(s/def ::pair (s/tuple ::any ::any))
(s/def ::set set?)
(s/def ::nil nil?)
(s/def ::number number?)
(s/def ::reducible reducible?)
(s/def ::seq seq?)
(s/def ::non-empty-seq (s/and ::seq not-empty))
(s/def ::vector vector?)
(s/def ::sequential sequential?)
(s/def ::some some?)
(s/def ::string string?)
(s/def ::char-sequence ::any)
;; FIXME: this doesn't work in spec-alpha2
#_#?(:clj (s/def ::char-sequence
          (s/with-gen
            #(instance? java.lang.CharSequence %)
            (fn []
              (gen/one-of (map #(gen/fmap %
                                          (s/gen ::string))
                               [#(StringBuffer. %)
                                #(StringBuilder. %)
                                #(java.nio.CharBuffer/wrap %)
                                #(String. %)]))))))

#_(defn seqable-of
  "every is not designed to deal with seqable?, this is a way around it"
  [spec]
  (s/with-gen (s/and seqable?
                     (s/or :empty empty?
                           :seq (s/and (s/conformer seq)
                                       (s/every spec))))
    #(s/gen (s/nilable (s/every spec :kind coll?)))))

(s/def ::seqable-of-map-entry ::any #_(seqable-of ::map-entry))


(s/def ::seqable-of-string ::any #_(seqable-of ::string))

(s/def ::seqable-of-nilable-string ::any #_(seqable-of (s/nilable ::string)))

(s/def ::regex-match (s/nilable
                      (s/or :string ::string
                            :seqable ::seqable-of-nilable-string)))

(s/def ::regex-matches ::any #_(seqable-of ::regex-match))

(s/def ::reducible-coll ::any)
;; FIXME: spec-alpha2
(s/def ::reducible-coll
  (s/with-gen
    (s/or
     :seqable    ::seqable
     :reducible  (s/nilable ::reducible)
     :iterable   (s/nilable ::iterable))
    #(s/gen ::seqable)))

(s/def ::coll coll?)
(s/def ::conjable (s/nilable ::coll))

(s/def ::java-coll
  (s/with-gen
    #(instance? java.util.Collection %)
    (fn []
      (gen/fmap #(java.util.ArrayList. %)
                (s/gen vector?)))))

(s/def ::predicate ::ifn)

(s/def ::transducer (s/with-gen
                      ::ifn
                      (fn []
                        (gen/return (map identity)))))

(s/def ::seqable-or-transducer
  (s/or :seqable ::seqable
        :transducer ::transducer))

;; FIXME: spec-alpha2
(s/def ::atom ::any)
#_(s/def ::atom
  (fn [a]
    #?(:clj (instance? clojure.lang.IAtom a)
       :cljs (satisfies? IAtom a))))

#?(:clj
   (defn regex? [r]
     (instance? java.util.regex.Pattern r))
   :cljs (def regex? cljs.core/regexp?))

(s/def ::regex
  (s/with-gen
    regex?
    (fn []
      (gen/fmap re-pattern
                (s/gen ::string)))))

;; FIXME: spec-alpha2
(s/def ::matcher ::any)
#_#?(:clj
   (s/def ::matcher
     #(instance? java.util.regex.Matcher %)))

(s/def ::sequential-of-non-sequential
  (s/every (complement sequential?) :kind sequential?))

(s/def ::non-empty-seqable
  (s/and ::seqable not-empty))

(s/def ::array
  (s/with-gen #?(:clj #(-> % .getClass .isArray)
                 :cljs array?)
    #(gen/one-of [(gen/fmap int-array (gen/vector (gen/int)))
                  (gen/fmap double-array (gen/vector (gen/double)))
                  #?(:clj (gen/fmap char-array (gen/string)))
                  (gen/fmap object-array (gen/vector (gen/any)))])))

(s/def ::indexed
  indexed?)

;; FIXME: spec-alpha2
(s/def ::nthable ::any)
#_(s/def ::nthable
  (s/with-gen (s/nilable (s/or :index ::indexed
                               :str #?(:clj ::char-sequence
                                       :cljs ::string)
                               :array ::array
                               #?@(:clj [:random-access #(instance? java.util.RandomAccess %)])
                               #?@(:clj [:matcher ::matcher])
                               :map-entry ::map-entry
                               :sequential ::sequential))
    #(gen/not-empty (gen/one-of [(s/gen ::indexed)
                                 #?(:clj (s/gen ::char-sequence)
                                    :cljs (gen/string))
                                 (s/gen ::array)
                                 (s/gen ::map-entry)
                                 (s/gen ::sequential)]))))

;; FIXME: spec-alpha2
(s/def ::stack ::any)
#_(s/def ::stack
  (s/with-gen
    #?(:cljs #(satisfies? IStack %)
       :clj #(instance? clojure.lang.IPersistentStack %))
    (fn []
      (s/gen (s/or :vector vector?
                   :list list?)))))

(s/def ::list list?)

;;;; Scratch

(comment
  (require '[clojure.spec-alpha2.test :as stest])
  (stest/instrument)
  (stest/unstrument))
