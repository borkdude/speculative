(ns speculative.core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(s/fdef clojure.core/map
  :args (s/cat :fn fn? :rest any?)
  :ret sequential?)

(stest/instrument `clojure.core/map)

