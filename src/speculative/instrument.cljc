(ns speculative.instrument
  "Loads all relevant speculative specs. Provides functions to only
  instrument and unstruments specs provided by speculative."
  (:require
   [clojure.string :as str]
   [speculative.core]
   [speculative.set]
   [speculative.string]
   [speculative.test :refer [deftime ?]])
  #?(:cljs
     (:require-macros
      [speculative.instrument :refer [instrument
                                      unstrument]])))

(def known-fdefs '[clojure.core/first
                   clojure.core/apply
                   clojure.core/assoc
                   clojure.core/count
                   clojure.core/swap!
                   clojure.core/reset!
                   clojure.core/juxt
                   clojure.core/every?
                   clojure.core/not-every?
                   clojure.core/partial
                   clojure.core/some
                   clojure.core/not-any?
                   clojure.core/map
                   clojure.core/filter
                   clojure.core/remove
                   clojure.core/range
                   clojure.core/merge
                   clojure.core/merge-with
                   clojure.core/re-pattern
                   #?(:clj clojure.core/re-matcher)
                   clojure.core/re-seq
                   clojure.core/subs
                   clojure.core/fnil
                   clojure.core/reduce])

(deftime
  (defmacro instrument []
    (let [known (mapv (fn [sym]
                        [sym]
                        (let [ns (namespace sym)
                              ns (? :cljs
                                    (str/replace ns #"^clojure\." "cljs.")
                                    :clj ns)
                              sym (symbol ns (name sym))]
                          (list 'quote sym)))
                      known-fdefs)]
      `(speculative.test/instrument ~known)))

  (defmacro unstrument []
    (let [known (mapv (fn [sym]
                        [sym]
                        (let [ns (namespace sym)
                              ns (? :cljs
                                    (str/replace ns #"^clojure\." "cljs.")
                                    :clj ns)
                              sym (symbol ns (name sym))]
                          (list 'quote sym)))
                      known-fdefs)]
      `(speculative.test/unstrument ~known))))
