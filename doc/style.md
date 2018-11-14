# Speculative style guide

All of these are style recommendations, no hard rules.

## Spec

Be as general as possible, while still being correct.

* Prefer named specs over predicates e.g. `::ifn` instead of `ifn?` [(#76)](https://github.com/slipset/speculative/issues/76)
* Use `associative?` instead of `map?` or `vector?` [(#46)](https://github.com/slipset/speculative/issues/46)
* Use `ifn?` instead of `fn?` [(#42)](https://github.com/slipset/speculative/issues/42)
* Use `seqable?` instead of `seq?` [(#45)](https://github.com/slipset/speculative/issues/45)


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

## Commit style

* Mention the Github issue number in the title
