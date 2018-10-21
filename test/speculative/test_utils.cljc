(ns speculative.test-utils
  (:require
   [clojure.string :as str]
   [clojure.test :as t :refer [deftest is testing]]
   [clojure.spec.alpha]
   [clojure.spec.test.alpha]
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

  (defmacro throws [symbol & body]
    `(let [msg#
           (net.cgrand.macrovich/case
               :clj (try ~@body
                         (catch clojure.lang.ExceptionInfo e#
                           (str e#)))
               :cljs (try ~@body
                          (catch js/Error e#
                            (str e#))))]
       (clojure.test/is (clojure.string/includes?
                         msg#
                         (str "Call to " (resolve ~symbol)
                              " did not conform to spec")))))

  )
