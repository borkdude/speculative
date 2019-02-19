(ns speculative.instrument
  "Loads all relevant speculative specs. Undefines specs that are not
  suited or useful to instrument and therefore makes it safe to
  call `(stest/instrument)`. Provides functions to only instrument and
  unstruments specs provided by speculative."
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [speculative.core]
   [speculative.set]
   [speculative.string]
   [speculative.impl :as impl]
   [speculative.impl.syms :as syms]
   [clojure.spec.test.alpha]
   #?(:cljs [goog.object :as gobj]))
  #?(:cljs
     (:require-macros
      [speculative.instrument :refer [instrument
                                      unstrument
                                      unload-blacklist]])))

(impl/deftime
  (defmacro unload-blacklist
    "Make it safe to call `(stest/instrument)` by unloading blacklisted
  specs. To undo unloading in the REPL, use `require` +
  `:reload`. Called when requiring this namespace."
    []
    (let [blacklist (impl/?
                     :clj speculative.impl.syms/blacklist-clj
                     :cljs speculative.impl.syms/blacklist-cljs)
          defs (for [sym blacklist]
                 (impl/?
                  :clj `(clojure.spec.alpha/def ~sym nil)
                  :cljs `(cljs.spec.alpha/def ~sym nil)))]
      `(do ~@defs
           '~blacklist)))
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

(impl/usetime
 (unload-blacklist))

;;;; Scratch

(comment
  (instrument))
