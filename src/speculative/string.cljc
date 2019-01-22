(ns speculative.string
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [speculative.specs :as ss]))

;; 180
(s/fdef str/join
  :args (s/cat :separator ::ss/any
               :coll ::ss/seqable)
  :ret ::ss/string)

;; 360
(s/fdef str/starts-with?
  :args (s/cat :cs #?(:clj  ::ss/char-sequence
                      :cljs ::ss/string)
               :substr ::ss/string)
  :ret ::ss/boolean)

;; 366
(s/fdef str/ends-with?
  :args (s/cat :cs #?(:clj  ::ss/char-sequence
                     :cljs ::ss/string)
               :substr ::ss/string)
  :ret ::ss/boolean)
