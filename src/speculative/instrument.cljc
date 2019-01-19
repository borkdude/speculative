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
   [speculative.impl.syms :as syms]
   [clojure.spec-alpha2.test :as stest])
  #?(:cljs
     (:require-macros
      [speculative.instrument :refer [instrument
                                      unstrument]])))

(impl/deftime

  (defmacro instrument []
    `(impl/instrument* ~(impl/?
                         :clj 'speculative.impl.syms/instrumentable-syms-clj
                         :cljs 'speculative.impl.syms/instrumentable-syms-cljs)))

  (defmacro unstrument []
    `(impl/unstrument* ~(impl/?
                         :clj 'speculative.impl.syms/instrumentable-syms-clj
                         :cljs 'speculative.impl.syms/instrumentable-syms-cljs))))

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
