(ns speculative.test
  "Macros and function utils for clojure.spec-alpha2.test. API may change
  at any time."
  (:require
   [clojure.spec-alpha2 :as s]
   [clojure.spec-alpha2.test :as stest]
   [clojure.test :as t :refer [deftest is testing]])
  #?(:cljs
     (:require-macros
      [speculative.test :refer [with-instrumentation
                                with-unstrumentation
                                throws
                                check-call
                                check]])))

;; deftime macro from https://github.com/cgrand/macrovich
(defmacro deftime
  [& body]
  (when #?(:clj (not (:ns &env))
           :cljs (when-let [n (and *ns* (ns-name *ns*))]
                   (re-matches #".*\$macros" (name n))))
    `(do ~@body)))

(deftime

  ;; case macro from https://github.com/cgrand/macrovich
  (defmacro ? [& {:keys [cljs clj]}]
    (if (contains? &env '&env)
      `(if (:ns ~'&env) ~cljs ~clj)
      (if #?(:clj (:ns &env) :cljs true)
        cljs
        clj)))

  ;; aliases so you don't have to require spec as clojure.spec-alpha2.test in cljs
  ;; before using this namespace, see #95
  (defmacro with-instrument-disabled [& body]
    `(? :clj
        (clojure.spec-alpha2.test/with-instrument-disabled ~@body)
        :cljs
        (cljs.spec.test.alpha/with-instrument-disabled ~@body)))

  (defmacro instrument [symbol]
    `(? :clj
        (clojure.spec-alpha2.test/instrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/instrument ~symbol)))

  (defmacro unstrument [symbol]
    `(? :clj
        (clojure.spec-alpha2.test/unstrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/unstrument ~symbol)))

  (defmacro get-spec [symbol]
    `(? :clj
        (clojure.spec.alpha/get-spec ~symbol)
        :cljs
        (cljs.spec.alpha/get-spec ~symbol)))

  (defmacro test-check [symbol opts]
    `(? :clj
        (clojure.spec-alpha2.test/check ~symbol ~opts)
        :cljs
        (cljs.spec.test.alpha/check ~symbol ~opts))))

(defn throwable? [e]
  (instance? #?(:clj Throwable
                :cljs js/Error) e))

(deftime

  (defmacro try-return
    "Executes body and returns exception as value"
    [& body]
    `(try ~@body
          (catch ~(? :clj 'Exception :cljs ':default) e#
            e#)))

  ;; with-(i/u)nstrumentation avoids using finally as a workaround for
  ;; https://dev.clojure.org/jira/browse/CLJS-2949
  (defmacro with-instrumentation
    "Executes body while instrumenting symbol."
    [symbol & body]
    `(let [was-instrumented?#
           (boolean
            (seq (unstrument ~symbol)))
           ret# (try-return
                 (instrument ~symbol)
                 ~@body)]
       (when-not was-instrumented?#
         (unstrument ~symbol))
       (if (throwable? ret#)
         (throw ret#)
         ret#)))

  (defmacro with-unstrumentation
    "Executes body while unstrumenting symbol."
    [symbol & body]
    `(let [was-instrumented?#
           (boolean
            (seq (unstrument ~symbol)))
           ret# (try-return
                 (unstrument ~symbol)
                 ~@body)]
       (when was-instrumented?#
         (instrument ~symbol))
       (if (throwable? ret#)
         (throw ret#)
         ret#)))

  (defmacro throws
    "Asserts that body throws spec error concerning s/fdef for symbol."
    [symbol & body]
    `(let [msg#
           (? :clj (try
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

(defn- explain-check
  [args spec v role]
  (ex-info
   "Specification-based check failed"
   (when-not (s/valid? spec v nil)
     (assoc (s/explain-data* spec [role] [] [] v)
            ::args args
            ::val v
            ::s/failure :check-failed))))

(defn do-check-call
  "Returns true if call passes specs, otherwise *returns* an exception
  with explain-data + ::s/failure."
  [f specs args]
  #_(println "SPECS" specs)
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
            ret))))))

(defn check-call*
  [f spec args]
  (let [ret (do-check-call f spec args)
        ex? (throwable? ret)]
    (if ex?
      (throw ret)
      ret)))

(deftime

  (defmacro check-call
    "Applies args to function resolved by symbol. Checks :args, :ret
  and :fn specs for spec resolved by symbol. Returns return value if check
  succeeded, else throws."
    [symbol args]
    (assert (vector? args))
    `(let [f# (resolve ~symbol)
           spec# (s/get-spec ~symbol)]
       (check-call* f# spec# ~args))))

(defn test-check-kw
  "Returns qualified keyword used for interfacing with
  clojure.test.check"
  [name]
  (keyword #?(:clj "clojure.spec.test.check"
              :cljs "clojure.test.check") name))

(defn successful?
  "Returns true if all spec.test.check tests have pass? true."
  [stc-result]
  (and (seq stc-result)
       (every? (fn [res]
                 (let [check-ret (get res (test-check-kw "ret"))]
                   (:pass? check-ret)))
               stc-result)))

(deftime

  (defmacro check
    "spec.test/check with third arg for passing clojure.test.check options."
    ([sym]
     `(gentest ~sym nil nil))
    ([sym opts tc-opts]
     `(with-instrument-disabled
        (println "generatively testing" ~sym)
        (let [opts# ~opts
              tc-opts# ~tc-opts
              opts# (update-in opts# [(test-check-kw "opts")]
                               (fn [o#]
                                 (merge o# tc-opts#)))
              ret#
              (test-check ~sym opts#)]
          ret#)))))

;;;; Scratch

(comment
  (check `count [nil])
  (check `some [1 1])
  (check `/ [1 1 1 1 1 1])
  (check `/ [0 0])
  )
