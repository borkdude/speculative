;; run with:

;; clj -Srepro -Sdeps '{:deps {org.clojure/clojurescript {:mvn/version "1.10.439"} org.clojure/test.check {:mvn/version "RELEASE"}}}' -m cljs.main -re node -i callstack.cljs

(ns callstack
  (:require
   [clojure.test.check]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t]
   ))

(s/fdef clojure.core/=
  :args (s/+ any?)
  :ret boolean?)

(t/deftest =-test
  (stest/instrument)
  (= 1))

(t/run-tests)
(println "done, exiting...")

