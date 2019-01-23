# Speculative contributor guidelines

## Thank you!

Thanks for taking the effort to contribute to this project and thanks for taking
the effort to read through this document first. It's appreciated.

## PR

* Don't include more than one spec or fix in a single PR.
* If you want to change existing specs, first read this document, previous
  issues and PRs related to those specs to inform yourself of prior discussions
  and decisions.
* Don't include changes unrelated to the purpose of the PR. This includes
  changing the project version number, adding lines to the .gitignore file, or
  changing the indentation or formatting.
* Don't open a new PR if changes are requested. Just push to the same branch and
  the PR will be updated.
* If you have any questions, you're welcome to discuss these in
  [#speculative](https://clojurians.slack.com/messages/CDJGJ3QVA) on [Clojurians
  Slack](http://clojurians.net/).
* Mention the Github issue number in the title of the commit.

## Specs

Be as general as possible, while still being correct.

* Prefer named specs over predicates e.g. `::ifn` instead of `ifn?` [(#76)](https://github.com/borkdude/speculative/issues/76)
* Use `associative?` instead of `map?` or `vector?` [(#46)](https://github.com/borkdude/speculative/issues/46)
* Use `ifn?` instead of `fn?` [(#42)](https://github.com/borkdude/speculative/issues/42)
* Use `s/alt` inside for arity alternatives in favor of `s/or`:

      :args (s/alt :infinite (s/cat ...) :finite (s/cat ...))

### Sequence functions

* Sequence conceptually accept and return `seqable?`, not `seq?`. Sequence
  functions compose because they call `seq` on their `seqable?` argument, not
  because they receive or return a `seq?`. Don't spec implementation details
  such as the difference between `()` and `nil` in return values. See
  [(#45)](https://github.com/borkdude/speculative/issues/45).

### Set functions

* See [#70](https://github.com/borkdude/speculative/issues/70),
  [#152](https://github.com/borkdude/speculative/pull/152) and
  [#161](https://github.com/borkdude/speculative/issues/161).

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

## Tests

Provide extensive tests for each spec.

A test for an `fdef` consists of thee parts:

1) Example based tests for arguments that should be accepted (must have)
2) Generative tests using `respeced.test/check` (nice to have)
3) Example based tests for arguments that should be rejected (must have)

Example:

``` clojure
(deftest interpose-test

  ;; 1) example based tests for arguments that should be accepted
  (is (check-call `interpose [0]))
  (is (check-call `interpose [0 [1 1 1]]))

  ;; 2) generative tests
  (check `interpose)

  ;; 3) example based tests for arguments that should be rejected
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

## Credits

Some of these guidelines have been inspired by the guidelines in
[medley](https://github.com/weavejester/medley/blob/e42cc45bab1b9e6a83284144f069cb7feabbb900/CONTRIBUTING.md).
