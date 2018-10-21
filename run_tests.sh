#!/bin/sh

clj -A:test:cljtests
clj -A:test:cljstests
plk -A:test:plktests
