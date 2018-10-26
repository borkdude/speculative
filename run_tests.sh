#!/bin/sh

clj -A:test:clj-tests
clj -A:test:cljs-tests
plk -A:test:plk-tests
