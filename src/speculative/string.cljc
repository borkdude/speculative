(ns speculative.string
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [speculative.specs :as ss]))

;; 360
(s/fdef str/starts-with?
  :args (s/cat :cs #?(:clj  ::ss/char-sequence
                      :cljs ::ss/string)
               :s ::ss/string)
  :ret ::ss/boolean)

;; 366
(s/fdef str/ends-with?
  :args (s/cat :cs #?(:clj  ::ss/char-sequence
                      :cljs ::ss/string)
               :s ::ss/string)
  :ret ::ss/boolean)
