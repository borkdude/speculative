(ns speculative.instrument
  "Instruments fns spec'ed by speculative"
  (:require [clojure.spec.test.alpha :as stest]
            [speculative.core]
            [speculative.set]
            [speculative.string]))

(let [instrumented (seq (stest/instrument))]
  (assert instrumented "No specs instrumented")
  (println "Speculative successfully instrumented\n"))
