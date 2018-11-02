;; run with:

;; clj -Srepro -Sdeps '{:deps {org.clojure/clojurescript {:git/url "https://github.com/clojure/clojurescript" :sha "6b9a37a294746148d3f4f8c1b6839823fe6e23f3"} org.clojure/test.check {:mvn/version "RELEASE"}}}' -m cljs.main -re node -i CLJS_spec_check_unstrument.cljc

(ns CLJS-repro
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [other]))

#_(s/fdef clojure.core/=
  :args (s/+ any?)
  :ret boolean?)

(deftest =-test
  (println "STEST CHECK RESULT:" (doall (stest/check `= {:clojure.test.check/opts {:num-tests 1}})))
  )

(t/run-tests)
