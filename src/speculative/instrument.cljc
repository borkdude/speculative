(ns speculative.instrument
  "Loads all relevant speculative specs. Provides functions to only
  instrument and unstruments specs provided by speculative. Alpha,
  subject to change."
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [speculative.core]
   [speculative.set]
   [speculative.string]
   [speculative.impl :as impl]
   [clojure.spec.test.alpha])
  #?(:cljs
     (:require-macros
      [speculative.instrument :refer [instrument
                                      unstrument]])))

(def instrumentable-syms
  `[;; clojure.core
    first
    rest
    last
    apply
    assoc
    count
    inc
    /
    dec
    swap!
    reset!
    juxt
    every?
    not-every?
    partial
    some
    not-any?
    map
    filter
    remove
    range
    merge
    merge-with
    re-pattern
    re-seq
    re-matches
    re-find
    subs
    interpose
    fnil
    reduce

    ;; clojure.string
    str/starts-with?
    str/ends-with?

    ;; clojure.set
    set/union
    set/intersection
    set/difference
    set/select
    set/project
    set/rename-keys
    set/rename
    set/index
    set/map-invert
    set/join
    set/subset?
    set/superset?])

(def instrumentable-syms-clj
  (into instrumentable-syms `[next re-matcher re-groups]))

(impl/deftime

  (defmacro instrument []
    (let [instrumentable-syms
          (impl/? :cljs instrumentable-syms :clj instrumentable-syms-clj)
          syms (mapv
                (fn [sym]
                  (let [ns (namespace sym)
                        ns (impl/? :cljs
                                   (str/replace ns #"^clojure\.core" "cljs.core")
                                   :clj ns)
                        sym (symbol ns (name sym))]
                    (list 'quote sym)))
                instrumentable-syms)]
      `(impl/instrument* ~syms)))

  (defmacro unstrument []
    (let [instrumentable-syms
          (impl/? :cljs instrumentable-syms :clj instrumentable-syms-clj)
          syms (mapv
                (fn [sym]
                  [sym]
                  (let [ns (namespace sym)
                        ns (impl/? :cljs
                                   (str/replace ns #"^clojure\.core" "cljs.core")
                                   :clj ns)
                        sym (symbol ns (name sym))]
                    (list 'quote sym)))
                instrumentable-syms)]
      `(impl/unstrument* ~syms))))

(defn fixture
  "Fixture that can be used with clojure.test"
  [test]
  (try
    (instrument)
    (test)
    (finally
      (unstrument))))

;;;; Scratch

(comment
  (instrument))
