#!/bin/sh

plk -A:test -e "(require '[clojure.test :as t])" -e "(require '[speculative.core-test])" -e "(t/run-tests 'speculative.core-test)"

clj -A:test:runner
