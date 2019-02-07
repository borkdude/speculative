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
   [clojure.spec.test.alpha]
   #?(:cljs [goog.object :as gobj]))
  #?(:cljs
     (:require-macros
      [speculative.instrument :refer [instrument
                                      unstrument
                                      unload-blacklist]])))

#?(:cljs (goog-define no-unload-blacklist? false))

(impl/usetime
 (defn ^:no-doc no-unload-blacklist??
   "Private."
   []
   #?(:clj (= "true" (System/getenv "SPECULATIVE_NO_UNLOAD_BLACKLIST"))
      :cljs (or (= "true" (cond (exists? js/process)
                                (gobj/get (gobj/get js/process "env")
                                          "SPECULATIVE_NO_UNLOAD_BLACKLIST")
                                (exists? js/PLANCK_GETENV)
                                (gobj/get (js/PLANCK_GETENV)
                                          "SPECULATIVE_NO_UNLOAD_BLACKLIST")
                                :else false))
                no-unload-blacklist?))))

(impl/deftime
  (defmacro unload-blacklist
    "Make it safe to call (stest/instrument) by unloading blacklisted
  specs. Respects environment variable
  `SPECULATIVE_NO_UNLOAD_BLACKLIST` or `goog-define`
  `no-unload-blacklist?`, unless `force?` is true. To undo unloading
  in the REPL, use `require` + `:reload`. Called when requiring this
  namespace."
    ([] `(unload-blacklist false))
    ([force?]
     (let [blacklist (impl/?
                      :clj speculative.impl.syms/blacklist-clj
                      :cljs speculative.impl.syms/blacklist-cljs)
           defs (for [sym blacklist]
                  (impl/?
                   :clj `(clojure.spec.alpha/def ~sym nil)
                   :cljs `(cljs.spec.alpha/def ~sym nil)))]
       `(when (or ~force? (not (no-unload-blacklist??)))
          ~@defs
          '~blacklist))))

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
