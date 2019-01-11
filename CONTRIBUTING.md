# Speculative contributor guidelines

## Spec

Be as general as possible, while still being correct.

* Prefer named specs over predicates e.g. `::ifn` instead of `ifn?` [(#76)](https://github.com/borkdude/speculative/issues/76)
* Use `associative?` instead of `map?` or `vector?` [(#46)](https://github.com/borkdude/speculative/issues/46)
* Use `ifn?` instead of `fn?` [(#42)](https://github.com/borkdude/speculative/issues/42)
* Use `seqable?` instead of `seq?` [(#45)](https://github.com/borkdude/speculative/issues/45)


* Use `s/alt` inside for arity alternatives in favor of `s/or`:

      :args (s/alt :infinite (s/cat ...) :finite (s/cat ...))

### Names

* Use consistent naming throughout the codebase.
* Try to use descriptive names for arguments, arities, etc. and use inspiriration from the docstring:

```
(s/fdef clojure.core/range
  :args (s/alt :infinite (s/cat)
               :finite (s/cat :start (s/? number?)
                              :end number?
                              :step (s/? number?)))
  :ret seqable?)
```

vs.

```
(s/fdef clojure.core/range
  :args (s/alt :arity-0 (s/cat)
               :arity-1-3 (s/cat :n1 (s/? number?)
                                 :n2 number?
                                 :n3 (s/? number?)))
  :ret seqable?)
```

## Code style

* Follow the Clojure [style guide](https://github.com/bbatsov/clojure-style-guide).
* Sort specs and tests by the way they appear in
  [clojure/core.clj](https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj)
  when possible.

## Test style

A test for an `fdef` consists of thee parts:

* Example based tests for cases that should be accepted (must have)
* Generative tests using `respeced.test/check` (nice to have)
* Example based tests for cases that should be rejected (must have)

Example:

``` clojure
(deftest interpose-test
  (is (check-call `interpose [0]))
  (is (check-call `interpose [0 [1 1 1]]))
  (check `interpose)
  (with-instrumentation `interpose
    (testing "wrong amount of args"
      (is (caught? `interpose (interpose))))
    (testing "non-coll arg"
      (is (caught? `interpose (interpose 0 0))))))
```

## Instrumentable syms

After writing a new spec, for it to be instrumentable using
`speculative.instrument/instrument`, run:

    script/update-syms

This will update the file `src/speculative/impl/syms.cljc`.

Some functions will not instrumentable on all environments or have no point in
being instrumented (e.g. `some?`, `str`). In that case you can update the
blacklist(s) in `blacklist.edn`.

## Commit style

* Mention the Github issue number in the title
