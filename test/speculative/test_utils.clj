(ns speculative.test-utils
  (:require [clojure.spec.test.alpha :as stest]))

(defmacro with-instrumentation [symbol & body]
  `(do (stest/instrument ~symbol)
       ~@body
       (stest/unstrument ~symbol)))
