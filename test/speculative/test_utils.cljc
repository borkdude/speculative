(ns speculative.test-utils
  (:require
   [clojure.string :as str]
   [clojure.test :as t :refer [deftest is testing]]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   #?(:clj [net.cgrand.macrovich :as macros]))
  #?(:cljs
     (:require-macros
      [net.cgrand.macrovich :as macros]
      [speculative.test-utils :refer [with-instrumentation
                                      throws]])))

(macros/deftime

  (defmacro with-instrumentation [symbol & body]
    `(do (clojure.spec.test.alpha/instrument ~symbol)
         (try ~@body
              (finally
                (clojure.spec.test.alpha/unstrument ~symbol)))))

  (defmacro throws [symbol expected-msg? & body]
    (let [string-opt? (string? expected-msg?)
          body (if string-opt?
                 body
                 `(do ~expected-msg? ~@body))]
      `(let [real-msg#
             (net.cgrand.macrovich/case
                 :clj (try ~@body
                           (catch clojure.lang.ExceptionInfo e#
                             (str e#)))
                 :cljs (try ~@body
                            (catch js/Error e#
                              (str e#))))]
         (clojure.test/is (clojure.string/includes?
                           real-msg#
                           (if ~string-opt?
                             ~expected-msg?
                             (str "Call to " (resolve ~symbol)
                                  " did not conform to spec"))))))))

(macros/usetime
 
 (defn check*
   [f spec args]
   (let [ret (#'stest/check-call f spec args)
         ex? #?(:clj (instance? clojure.lang.ExceptionInfo ret)
                :cljs (instance? cljs.core/ExceptionInfo ret))]
     (if ex?
       (throw ret)
       ret))))

(macros/deftime

  (defmacro check
    [symbol args]
    (assert (vector? args))
    `(let [f# (resolve ~symbol)
           spec# (s/get-spec ~symbol)]
       (check* f# spec# ~args))))


;;;; Scratch

(comment
  (check `count [nil])
  )
