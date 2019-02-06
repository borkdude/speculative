(ns ^:no-doc speculative.impl
  #?(:cljs (:require-macros
            [speculative.impl :refer [deftime usetime ? instrument* unstrument*]])))

(defmacro deftime
  "Private. deftime macro from https://github.com/cgrand/macrovich"
  [& body]
  (when #?(:clj (not (:ns &env))
           :cljs (when-let [n (and *ns* (ns-name *ns*))]
                   (re-matches #".*\$macros" (name n))))
    `(do ~@body)))

(deftime
  (defmacro usetime
    "Private. usetime macro from https://github.com/cgrand/macrovich"
    [& body]
    (when #?(:clj true :cljs (not (re-matches #".*\$macros" (name (ns-name *ns*)))))
      `(do ~@body)))

  (defmacro ?
    "Private. case macro from https://github.com/cgrand/macrovich"
    [& {:keys [cljs clj]}]
    (if (contains? &env '&env)
      `(if (:ns ~'&env) ~cljs ~clj)
      (if #?(:clj (:ns &env) :cljs true)
        cljs
        clj)))

  (defmacro instrument*
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec-alpha2.test/instrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/instrument ~symbol)))

  (defmacro unstrument*
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec-alpha2.test/unstrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/unstrument ~symbol))))

