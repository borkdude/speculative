(ns speculative.instrument
  "Instruments fns spec'ed by speculative"
  (:require [clojure.spec-alpha2.test :as stest]
            [speculative.core]
            [speculative.set]
            [speculative.string]))

(let [instrumented (seq (stest/instrument))]
  (assert instrumented "No specs instrumented")
  (println "Speculative successfully instrumented\n"))
