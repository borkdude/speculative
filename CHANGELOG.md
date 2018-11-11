## 0.2.1-SNAPSHOT

* Stricter `merge-with` spec:

``` clojure
user=> (merge-with assoc {:a 1} [:a :b])
Execution error (ClassCastException) at user$eval164/invokeStatic (REPL:1).
clojure.lang.Keyword cannot be cast to java.util.Map$Entry
```
becomes

``` clojure
user=> (merge-with + {:a 1} [:a :b])
Evaluation error - invalid arguments to clojure.core/merge-with at (NO_SOURCE_FILE:15).
:a - failed: map-entry? at: [:maps :rest-maps] spec: :speculative.core/map-entry
:b - failed: map-entry? at: [:maps :rest-maps] spec: :speculative.core/map-entry
```

* `speculative.test` macros `gentest` and `check` renamed to `check` and `check-call` to closer resemble naming in `clojure.spec.test.alpha`
* `speculative.test` no longer needs require to `clojure.spec.test.alpha` in CLJS ([#95](https://github.com/slipset/speculative/issues/95))

## 0.2.0 (2018-11-09)

* Specs for `=`, `/`, `apply` (clj only), `assoc`, `count`, `every?`, `filter`,
  `first`, `get`, `juxt`, `not-any?`, `not-every?`, `range`, `partial`,
  `remove`, `reset!`, `swap!`, `some`, `some?` and `str`.
* Namespace `speculative.test` with tools around `clojure.spec.test.alpha`. More
  info [here](doc/test.md).

## 0.1.0 (2018-10-20)

* Initial release with specs for `map`, `filter`, `merge`, `merge-with`, `fnil`
  and `reduce`.
