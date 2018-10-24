(ns speculative.test-utils
  (:require
   [clojure.string :as str]
   [clojure.test :as t :refer [deftest is testing]]
   [clojure.spec.alpha]
   #?(:clj [orchestra.spec.test :as stest]
      :cljs [orchestra-cljs.spec.test :as stest])
   #_[clojure.spec.test.alpha]
   #?(:clj [net.cgrand.macrovich :as macros]))
  #?(:cljs
     (:require-macros
      [net.cgrand.macrovich :as macros]
      [speculative.test-utils :refer [with-instrumentation
                                      throws]])))

(macros/deftime

  (defmacro with-instrumentation [symbol & body]
    `(do (orchestra-cljs.spec.test/instrument ~symbol)
         (try ~@body
              (finally
                (orchestra-cljs.spec.test/unstrument ~symbol)))))

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
