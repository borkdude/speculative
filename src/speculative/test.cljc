(ns speculative.test
  "Macros and function utils for clojure.spec.test.alpha and
  clojure.test. Alpha, subject to change."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]])
  #?(:cljs
     (:require-macros
      [speculative.test :refer [with-instrumentation
                                with-unstrumentation
                                throws
                                check-call
                                check]])))

;;;; Implementation

(defmacro deftime
  "Private. deftime macro from https://github.com/cgrand/macrovich"
  [& body]
  (when #?(:clj (not (:ns &env))
           :cljs (when-let [n (and *ns* (ns-name *ns*))]
                   (re-matches #".*\$macros" (name n))))
    `(do ~@body)))

(deftime

  (defmacro ?
    "Private. case macro from https://github.com/cgrand/macrovich"
    [& {:keys [cljs clj]}]
    (if (contains? &env '&env)
      `(if (:ns ~'&env) ~cljs ~clj)
      (if #?(:clj (:ns &env) :cljs true)
        cljs
        clj)))

  ;; aliases so you don't have to require spec as clojure.spec.test.alpha in cljs
  ;; before using this namespace, see #95
  (defmacro with-instrument-disabled
    "Private."
    [& body]
    `(? :clj
        (clojure.spec.test.alpha/with-instrument-disabled ~@body)
        :cljs
        (cljs.spec.test.alpha/with-instrument-disabled ~@body)))

  (defmacro instrument
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec.test.alpha/instrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/instrument ~symbol)))

  (defmacro unstrument
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec.test.alpha/unstrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/unstrument ~symbol)))

  (defmacro get-spec
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec.alpha/get-spec ~symbol)
        :cljs
        (cljs.spec.alpha/get-spec ~symbol)))

  (defmacro test-check
    "Private."
    [symbol opts]
    `(? :clj
        (clojure.spec.test.alpha/check ~symbol ~opts)
        :cljs
        (cljs.spec.test.alpha/check ~symbol ~opts))))

(defn throwable?
  "Private."
  [e]
  (instance? #?(:clj Throwable
                :cljs js/Error) e))

(deftime

  (defmacro try-return
    "Private. Executes body and returns exception as value"
    [& body]
    `(try ~@body
          (catch ~(? :clj 'Exception :cljs ':default) e#
            e#))))

(defn do-check-call
  "Private. From clojure.spec.test.alpha, adapted for speculative."
  [f specs args]
  (clojure.spec.test.alpha/with-instrument-disabled
    (let [cargs (when (:args specs) (s/conform (:args specs) args))]
      (if (= cargs ::s/invalid)
        (#'clojure.spec.test.alpha/explain-check args (:args specs) args :args)
        (let [ret (apply f args)
              cret (when (:ret specs) (s/conform (:ret specs) ret))]
          (if (= cret ::s/invalid)
            (#'clojure.spec.test.alpha/explain-check args (:ret specs) ret :ret)
            (if (and (:args specs) (:ret specs) (:fn specs))
              (if (clojure.spec.alpha/valid? (:fn specs) {:args cargs :ret cret})
                ret
                (#'clojure.spec.test.alpha/explain-check args (:fn specs) {:args cargs :ret cret} :fn))
              ret)))))))

(defn check-call*
  "Private."
  [f spec args]
  (let [ret (do-check-call f spec args)
        ex? (throwable? ret)]
    (if ex?
      (throw ret)
      ret)))

(defn test-check-kw
  "Private. Returns qualified keyword used for interfacing with
  clojure.test.check"
  [name]
  (keyword #?(:clj "clojure.spec.test.check"
              :cljs "clojure.test.check") name))

(defn planck-env? []
  #?(:cljs (exists? js/PLANCK_EXIT_WITH_VALUE)
     :clj false))


#?(:clj
   (defn run-tests*
     "Private. run-tests from clojure.test with fix for CLJ-2443"
     ([] (t/run-tests *ns*))
     ([& namespaces]
      (let [summary (assoc (apply merge-with + (mapv t/test-ns namespaces))
                           :type :summary)]
        (t/do-report summary)
        summary))))

(deftime

  (defmacro run-tests
    ([] (t/run-tests))
    ([& namespaces]
     `(?
       :clj (run-tests* ~@namespaces)
       :cljs (cljs.test/run-tests ~@namespaces)))))

;;;; Public API

(deftime

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
                              " did not conform to spec")))))

  (defmacro check-call
    "Applies args to function resolved by symbol. Checks :args, :ret
  and :fn specs for spec resolved by symbol. Returns return value if check
  succeeded, else throws."
    [symbol args]
    (assert (vector? args))
    `(let [f# (resolve ~symbol)
           spec# (get-spec ~symbol)]
       (check-call* f# spec# ~args)))

  (defmacro check
    "spec.test/check with third arg for passing clojure.test.check options."
    ([sym]
     `(check ~sym nil nil))
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

(defn successful?
  "Returns true if all spec.test.alpha/check tests have pass? true."
  [stc-result]
  (and (seq stc-result)
       (every? (fn [res]
                 (let [check-ret (get res (test-check-kw "ret"))]
                   (:pass? check-ret)))
               stc-result)))

;;;; Scratch

(comment
  (check `count [nil])
  (check `some [1 1])
  (check `/ [1 1 1 1 1 1])
  (check `/ [0 0])
  )
