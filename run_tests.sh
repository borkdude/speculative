#!/bin/sh

clj -A:test:runner
clj -A:test:cljstests
plk -A:test:plktests
