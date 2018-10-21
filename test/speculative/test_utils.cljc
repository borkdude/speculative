(ns speculative.test-utils
  (:require
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.test :as t :refer [deftest is testing]]
   [clojure.spec.test.alpha :as stest]
   #?(:clj [net.cgrand.macrovich :as macros]))
  #?(:cljs
     (:require-macros
      [net.cgrand.macrovich :as macros]
      [speculative.test-utils :refer [with-instrumentation
                                      throws]])))

(macros/deftime

  (defmacro with-instrumentation [symbol & body]
    `(do (stest/instrument ~symbol)
         (try ~@body
              (finally
                (stest/unstrument ~symbol)))))

  (defmacro throws [symbol & body]
    `(let [msg#
           (macros/case :clj (try ~@body
                                  (catch clojure.lang.ExceptionInfo e#
                                    (str e#)))
                        :cljs (try ~@body
                                   (catch js/Error e#
                                     (str e#))))]
       (clojure.test/is (str/includes?
                         msg#
                         (str "Call to " (resolve ~symbol)
                              " did not conform to spec")))))

  )
