(ns speculative.test-prelude
  (:require [clojure.test :as t]))

#?(:cljs
   (defmethod cljs.test/report [:cljs.test/default :begin-test-var] [m]
     ;; for debugging:
     #_(println ":begin-test-var" (cljs.test/testing-vars-str m))))
