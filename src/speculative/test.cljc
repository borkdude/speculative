(ns speculative.test
  "Macros and function utils for clojure.spec.test.alpha. API may change
  at any time."
  (:require
   [clojure.string :as str]
   [clojure.test :as t :refer [deftest is testing]]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest])
  #?(:cljs
     (:require-macros
      [speculative.test :refer [with-instrumentation
                                with-unstrumentation
                                choose-env
                                throws
                                check
                                gentest]])))

;; deftime macro from https://github.com/cgrand/macrovich
(defmacro deftime
  [& body]
  (when #?(:clj (not (:ns &env))
           :cljs (when-let [n (and *ns* (ns-name *ns*))]
                   (re-matches #".*\$macros" (name n))))
    `(do ~@body)))

;; case macro from https://github.com/cgrand/macrovich
(deftime
  (defmacro choose-env [& {:keys [cljs clj]}]
    (if (contains? &env '&env)
      `(if (:ns ~'&env) ~cljs ~clj)
      (if #?(:clj (:ns &env) :cljs true)
        cljs
        clj))))

(defn throwable? [e]
  (instance? #?(:clj Throwable
                :cljs js/Error) e))

(deftime
  ;; with-(i/u)nstrumentation avoids using finally as a workaround for
  ;; https://dev.clojure.org/jira/browse/CLJS-2949
  (defmacro with-instrumentation
    "Executes body while instrumenting symbol."
    [symbol & body]
    `(let [was-instrumented?#
           (boolean
            (seq (clojure.spec.test.alpha/unstrument ~symbol)))]
       (let [ret# (try (clojure.spec.test.alpha/instrument ~symbol)
                       ~@body
                       (catch #?(:clj Exception :cljs :default) e#
                         e#))]
         (when-not was-instrumented?#
           (clojure.spec.test.alpha/unstrument ~symbol))
         (if (throwable? ret#)
           (throw ret#)
           ret#))))

  (defmacro with-unstrumentation
    "Executes body while unstrumenting symbol."
    [symbol & body]
    `(let [was-instrumented?#
           (boolean
            (seq (clojure.spec.test.alpha/unstrument ~symbol)))]
       (let [ret# (try (clojure.spec.test.alpha/unstrument ~symbol)
                       ~@body
                       (catch #?(:clj Exception :cljs :default) e#
                         e#))]
         (when was-instrumented?#
           (clojure.spec.test.alpha/instrument ~symbol))
         (if (throwable? ret#)
           (throw ret#)
           ret#))))

  (defmacro throws
    "Asserts that body throws spec error concerning s/fdef for symbol."
    [symbol & body]
    `(let [msg#
           (choose-env :clj (try
                              ~@body
                              (catch clojure.lang.ExceptionInfo e#
                                (.getMessage e#)))
                       :cljs (try
                               ~@body
                               (catch js/Error e#
                                 (.-message e#))))]
       (clojure.test/is (clojure.string/starts-with?
                         msg#
                         (str "Call to " (resolve ~symbol)
                              " did not conform to spec"))))))

(def ^:private explain-check #'stest/explain-check)

(defn check-call
  "From clojure.spec.test.alpha, adapted for speculative."
  [f specs args]
  (stest/with-instrument-disabled
    (let [cargs (when (:args specs) (s/conform (:args specs) args))]
      (if (= cargs ::s/invalid)
        (explain-check args (:args specs) args :args)
        (let [ret (apply f args)
              cret (when (:ret specs) (s/conform (:ret specs) ret))]
          (if (= cret ::s/invalid)
            (explain-check args (:ret specs) ret :ret)
            (if (and (:args specs) (:ret specs) (:fn specs))
              (if (s/valid? (:fn specs) {:args cargs :ret cret})
                ret
                (explain-check args (:fn specs) {:args cargs :ret cret} :fn))
              ret)))))))

(defn check*
  [f spec args]
  (let [ret (check-call f spec args)
        ex? #?(:clj (instance? clojure.lang.ExceptionInfo ret)
               :cljs (instance? cljs.core/ExceptionInfo ret))]
    (if ex?
      (throw ret)
      ret)))

(deftime
  (defmacro check
    "Applies args to function resolved by symbol. Checks :args, :ret
  and :fn specs for spec resolved by symbol. Returns return value if check
  succeeded, else throws."
    [symbol args]
    (assert (vector? args))
    `(let [f# (resolve ~symbol)
           spec# (s/get-spec ~symbol)]
       (check* f# spec# ~args))))

(defn test-check-kw
  "Returns qualified keyword used for interfacing with
  clojure.test.check"
  [name]
  (keyword #?(:clj "clojure.spec.test.check"
              :cljs "clojure.test.check") name))

(defn success?
  "Returns true if all spec.test.check tests have pass? true."
  [stc-result]
  (and (seq stc-result)
       (every? (fn [res]
                 (let [check-ret (get res (test-check-kw "ret"))]
                   (:pass? check-ret)))
               stc-result)))

(deftime
  (defmacro gentest
    "spec.test/check with third arg for passing clojure.test.check options."
    ([sym]
     `(gentest ~sym nil nil))
    ([sym opts tc-opts]
     `(clojure.spec.test.alpha/with-instrument-disabled
        (println "generatively testing" ~sym)
        (let [opts# ~opts
              tc-opts# ~tc-opts
              opts# (update-in opts# [(test-check-kw "opts")]
                               (fn [o#]
                                 (merge o# tc-opts#)))
              ret#
              (clojure.spec.test.alpha/check ~sym opts#)]
          ret#)))))

;;;; Scratch

(comment
  (check `count [nil])
  (check `some [1 1])
  (check `/ [1 1 1 1 1 1])
  (check `/ [0 0])
  )
