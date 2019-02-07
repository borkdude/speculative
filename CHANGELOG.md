## 0.0.3-SNAPSHOT

### New specs

This release comes with 73 new specs:

``` clojure
* + - assoc-in atom comp conj cons dec dedupe dissoc distinct every-pred find
flatten frequencies get-in group-by hash-map hash-set inc interpose into keep
keys last list max max-key min min-key next nil?  not nth partition
partition-all partition-by peek pop re-find re-groups re-matcher re-matches
re-pattern re-seq rest select-keys seq shuffle some-fn subs update update-in
vals vec vector zipmap set/difference set/index set/intersection set/join
set/map-invert set/project set/rename set/rename-keys set/select set/subset?
set/superset?  set/union string/ends-with?  string/join string/starts-with?
```

Several existing specs were improved.

### New logo

Speculative has a shiny new logo. See the [README](README.md) and [logo](logo)
directory.

### Namespace `speculative.instrument`

Namespace `speculative.instrument` loads `speculative.core`, `speculative.set`
and `speculative.string`, so you don't have to. When loading this namespace,
specs that are not suited or useful for instrumentation are undefined. This
makes calling `(stest/instrument)` safe, since some specs can cause errors when
instrumented (e.g. `hash-map` on CLJ and `apply` and `seq` on CLJS).

This namespace also provides `instrument` and `unstrument` functions to only
i/unstrument specs provided by speculative.

To make testing easier, this namespace provides a `clojure.test` `fixture` that
wraps a test with speculative instrumentation.

Turning off specs that are not useful to instrument is beneficial for
performance. Compare running the first 20 coal-mine test sets in CLJS on node:

Without instrumentation of `some?`, `str`, `=` and `get`:
``` shell
"Elapsed time: 3198.731217 msecs"
```
With instrumentation of said functions:
``` shell
"Elapsed time: 21343.952922 msecs"
```

### Library `respeced`

The namespace `speculative.test` was promoted to a library called
[respeced](https://github.com/borkdude/respeced) and therefore removed from
speculative.

### Contributors

Thanks to the contributors in this release:

- Andreas Liljeqvist (@bonega): provided several new specs, improvements around
  blacklisting of specs and testing of individual specs.
- Eero Helenius (@eerohele): provided specs for the entire `clojure.set` namespace.
- Nikita Prokopov (@tonsky): provided the logo for speculative.

Additional thanks to Alex Miller (@puredanger) and Andy Fingerhut (@jafingerhut)
for providing feedback on several issues.

## 0.0.2 (2018-11-09)

### New specs:

``` clojure
= / apply assoc count every?  filter first fnil get juxt map merge merge-with
not-any?  not-every?  range partial reduce remove reset!  some some?  str swap!
```

### Namespace `speculative.test`

Namespace `speculative.test` with tools around `clojure.spec.test.alpha`. More
info [here](doc/test.md).

## 0.0.1 (2018-10-20)

Initial release with specs for `map`, `filter`, `merge`, `merge-with`, `fnil`
and `reduce`.
