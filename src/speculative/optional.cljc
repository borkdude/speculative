(ns speculative.optional
  "This namespace contains optional core specs that are not likely to
  find errors while instrumented."
  (:require [clojure.spec.alpha :as s]
            [speculative.core :as sp]))

(comment
  = ;; no way to pass wrong args (except 0-arity)
  / ;; inlined
  get ;; no way to pass wrong args (except 0-arity)
  some? ;; no way to pass wrong args (except 0-arity)
  str ;; no way to pass wrong args
)

(s/fdef clojure.core/=
  :args (s/+ ::sp/any)
  :ret ::sp/boolean)

;; inlined
(s/fdef clojure.core//
  :args (s/cat :numerator ::sp/number
               :denominators (s/* ::sp/number))
  :ret ::sp/number)

(s/fdef clojure.core/get
  :args (s/cat :map ::sp/any
               :key ::sp/any
               :default (s/? ::sp/any))
  :ret ::sp/any)

(s/fdef clojure.core/some?
  :args (s/cat :x ::sp/any)
  :ret ::sp/any)

(s/fdef clojure.core/str
  :args (s/* ::sp/any)
  :ret ::sp/string)

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument)
  )
