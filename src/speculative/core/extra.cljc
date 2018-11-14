(ns speculative.core.extra
  "This namespace contains optional core specs that are not likely to
  find errors while instrumented."
  (:require
   [clojure.spec-alpha2 :as s]
   [speculative.specs :as ss]))

(comment
  = ;; no way to pass wrong args (except 0-arity)
  / ;; inlined
  get ;; no way to pass wrong args (except 0-arity)
  some? ;; no way to pass wrong args (except 0-arity)
  str ;; no way to pass wrong args
  )

;; 531
(s/fdef clojure.core/some?
  :args (s/cat :x ::ss/any)
  :ret ::ss/any)

;; 544
(s/fdef clojure.core/str
  :args (s/* ::ss/any)
  :ret ::ss/string)

;; 783
(s/fdef clojure.core/=
  :args (s/+ ::ss/any)
  :ret ::ss/boolean)

;; 1020
(s/fdef clojure.core//
  :args (s/cat :numerator ::ss/number
               :denominators (s/* ::ss/number))
  :ret ::ss/number)

;; 1494
(s/fdef clojure.core/get
  :args (s/cat :map ::ss/any
               :key ::ss/any
               :default (s/? ::ss/any))
  :ret ::ss/any)

;;;; Scratch

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (stest/unstrument)
  )
