(ns speculative.specs
  "Primitive specs"
  (:refer-clojure :exclude [seqable? reduceable? regexp?])
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as str]
   [com.gfredericks.test.chuck.generators :as gen']
   #?(:cljs [goog.string])))

#?(:cljs (def ^:private
           before-1_10_439?
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
(s/def ::counted (s/with-gen counted?
                   #(s/gen (s/spec seqable?))))
(s/def ::ifn ifn?)
(s/def ::predicate
  (s/with-gen ::ifn
    (fn [] (gen/bind (s/gen ::boolean)
                     (fn [b] (gen/return (fn [x] b)))))))

(s/def ::int int?)
(s/def ::nat-int nat-int?)
(s/def ::pos-int pos-int?)
(s/def ::iterable iterable?)
(s/def ::map map?)
#?(:clj (s/def ::java-map
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
(s/def ::keyword keyword?)

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
(s/def ::string+ #?(:cljs ::string :clj ::char-sequence))

(defn seqable-of
  "every is not designed to deal with seqable?, this is a way around it"
  [spec]
  (s/with-gen (s/and seqable?
                     (s/or :empty empty?
                           :seq (s/and (s/conformer seq)
                                       (s/every spec))))
    ;; avoid generation of strings and vectors (those are interpreted as pairs when using conj with maps
    #(s/gen (s/nilable (s/every spec :kind seq?)))))

(s/def ::seqable-of-map-entry (seqable-of ::map-entry))

(s/def ::seqable-of-nilable-string (seqable-of (s/nilable ::string)))

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

(s/def ::transducer (s/with-gen
                      ::ifn
                      (fn []
                        (gen/return (map identity)))))

(s/def ::seqable-or-transducer
  (s/or :seqable ::seqable
        :transducer ::transducer))

;;;; Atoms

(s/def ::atom
  (s/with-gen
    (fn [a]
      #?(:clj (instance? clojure.lang.IAtom a)
         :cljs (satisfies? IAtom a)))
    #(gen/fmap (fn [any]
                 (atom any))
               (s/gen ::any))))

(s/def :speculative.atom.options/validator
  (s/with-gen ::predicate
    (fn [] (gen/return (fn [_] true)))))
(s/def :speculative.atom.options/meta ::map)
(s/def ::atom.options
  (s/keys* :opt-un [:speculative.atom.options/validator
                    :speculative.atom.options/meta]))

;;;; End Atoms

;;;; Regex stuff

#?(:clj
   (defn regex? [r]
     (instance? java.util.regex.Pattern r))
   :cljs (def regex? cljs.core/regexp?))

(s/def ::regex.gen.sub-pattern
  (s/cat :pattern
         (s/alt :chars (s/+ #{\a \b})
                :group (s/cat :open-paren #{\(}
                              :inner-pattern ::regex.gen.sub-pattern
                              :closing-paren #{\)}))
         :maybe (s/? #{\?})))

(s/def ::regex.gen.pattern (s/coll-of ::regex.gen.sub-pattern :gen-max 10))

(def regex-gen
  (gen/fmap
   (fn [patterns]
     (let [s (reduce #(str %1 (str/join %2)) "" patterns)]
       (re-pattern s)))
   (s/gen ::regex.gen.pattern)))

(def regex-with-string-gen
  "Returns generator that generates a regex and a string that will match
  90% of the time on CLJ and will maybe match 10% of the time on
  CLJ. On CLJS it will maybe match 100% of the time, since the string
  from regex generator isn't used there."
  (gen/bind
   regex-gen
   (fn [re]
     (gen/tuple
      (gen/return re)
      (gen/frequency
       [#?(:clj [9
                 (gen'/string-from-regex re)])
        [#?(:clj 1 :cljs 10)
         (gen/fmap str/join (s/gen (s/* #{\a \b})))]])))))

#?(:clj
   (do
     (def regex-with-matching-string-gen
       (gen/bind regex-gen 
        (fn [re]
          (gen/tuple (gen/return re)
                     (gen'/string-from-regex re)))))

     (def matching-matcher-gen
       (gen/fmap (fn [[r s]]
                   (re-matcher r s))
                 regex-with-matching-string-gen))

     (def matcher-gen
       (gen/fmap (fn [[r strs]]
                   (re-matcher r (str/join strs)))
                 regex-with-string-gen))

     (s/def ::matcher
       (s/with-gen #(instance? java.util.regex.Matcher %)
         (fn [] matcher-gen)))

     ;; test matcher-gen:
     (comment
       (re-find (gen/generate matcher-gen)))))

(s/def ::regex+string-args
  (s/with-gen (s/cat :re ::regex :s ::string+)
    (fn [] regex-with-string-gen)))

(s/def ::regex
  (s/with-gen
    regex?
    (fn [] regex-gen)))

(s/def ::regex-match (s/nilable
                      (s/or :string ::string
                            :seqable ::seqable-of-nilable-string)))

(s/def ::regex-matches (seqable-of ::regex-match))

;;;; End regex stuff

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

(s/def ::nthable
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

(s/def ::stack
  (s/with-gen
    #?(:cljs #(satisfies? IStack %)
       :clj #(instance? clojure.lang.IPersistentStack %))
    (fn []
      (s/gen (s/or :vector vector?
                   :list list?)))))

(s/def ::list list?)

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument))
