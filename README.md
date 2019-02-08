<img src="./logo/favicon-160.png">

# speculative

[![CircleCI](https://circleci.com/gh/borkdude/speculative/tree/master.svg?style=svg)](https://circleci.com/gh/borkdude/speculative/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/speculative.svg)](https://clojars.org/speculative)
[![cljdoc badge](https://cljdoc.org/badge/speculative/speculative)](https://cljdoc.org/d/speculative/speculative/CURRENT)

Unofficial community-driven specs for `clojure.core`.

## Quickstart

``` clojure
Clojure 1.10.0
user=> (require '[speculative.instrument :as i])
nil
user=> (i/instrument)
[clojure.core/every-pred clojure.core/max clojure.string/join ...]
user=> (subs "foo" -1)
Execution error - invalid arguments to clojure.core/subs at (REPL:1).
-1 - failed: nat-int? at: [:start] spec: :speculative.specs/nat-int
```

## Rationale

By writing, using and maintaining core specs, and reflecting upon them, we get
the following benefits:

* Better error messages during development and testing
* Discover where Clojure and ClojureScript functions behave differently and when
  possible fix it
* Discover the established usages of functions within the Clojure community
* Provide data for [re-find](https://re-find.it), an app that helps you find
  functions using specs

## Disclaimer

These specs reflect what we currently know about the newest versions of Clojure
and ClojureScript. These specs are in no way definitive or authoritative. They
may evolve based on new insights and changes in Clojure. These specs have no
official status, are not endorsed by Cognitect and are provided without
warranty.

## Installation

### Tools.deps

``` clojure
{:deps {speculative {:mvn/version "0.0.3"}}}
```

### Leiningen / Boot

``` clojure
[speculative "0.0.3"]
```

## Usage

Speculative specs correspond to the namespaces in Clojure:

``` clojure
speculative.core   -> clojure.core
speculative.set    -> clojure.set
speculative.string -> clojure.string
```

To load all specs at once, you can require `speculative.instrument` which also
provides functions to only instrument speculative specs.

``` clojure
$ clj
Clojure 1.10.0
user=> (require '[speculative.instrument :refer [instrument unstrument]])
nil
user=> (instrument)
[clojure.core/every-pred clojure.core/max clojure.string/join ...]

user=> (merge-with 1 {:a 2} {:a 3})
Execution error - invalid arguments to clojure.core/merge-with at (REPL:1).
1 - failed: ifn? at: [:f] spec: :speculative.specs/ifn

user=> (unstrument)
...
user=> (merge-with 1 {:a 2} {:a 3})
Execution error (ClassCastException) at user$eval344/invokeStatic (REPL:1).
java.lang.Long cannot be cast to clojure.lang.IFn
```

### Usage in testing

To instrument during testing, you can use the `fixture` from
`speculative.instrument`:

``` clojure
(require '[clojure.test :as t])
(require '[speculative.instrument :as i])
(t/use-fixtures :once i/fixture)
```

This will turn on instrumentation before the tests and turn it off after.

If you run tests with [kaocha](https://github.com/lambdaisland/kaocha) you can
use the [speculative kaocha
plugin](https://github.com/borkdude/speculative-kaocha-plugin).

## Speculative broke my project

Speculative specs find, when instrumented, invalid or undefined usage of Clojure
core functions. If code is under your control, you can fix it. If the call was
made in a library not under your control, you can unstrument the spec using
`clojure.spec.test.alpha/unstrument`, unload it using `(s/def spec-symbol nil)`
or disable it within the scope of a body using
`respeced.test/with-unstrumentation` (see
[respeced](https://github.com/borkdude/respeced)):

``` clojure
$ clj
Clojure 1.10.0
user=> (require '[respeced.test :refer [with-unstrumentation]])
nil
user=> (require '[speculative.instrument :refer [instrument]])
nil
user=> (instrument)
[clojure.core/first clojure.core/apply clojure.core/assoc ...]
user=> (merge #{1 2 3} 4)
Execution error - invalid arguments to clojure.core/merge at (REPL:1).
#{1 3 2} - failed: map? at: [:maps :init-map :clojure.spec.alpha/pred]
#{1 3 2} - failed: nil? at: [:maps :init-map :clojure.spec.alpha/nil]
user=> (respeced.test/with-unstrumentation `merge (merge #{1 2 3} 4))
#{1 4 3 2}
```

If you believe the spec was wrong, please create an
[issue](https://github.com/borkdude/speculative/issues).

## Tests

### Clojure

    clj -A:test:clj-tests
     
### ClojureScript (Node)

    script/cljs-tests
    
### Self-hosted ClojureScript (Planck)
   
    plk -A:test:plk-tests

### Number of generative tests

By default the number of generative tests is set to `50`, but this can be
overriden by setting the environment variable `NUM_TESTS`:

    NUM_TESTS=1001 clj -A:test:clj-tests

### Run a single test

#### Clojure

    clojure -A:test:clj-test-runner -v speculative.core-test/assoc-in-test

#### ClojureScript (Node)

    clojure -A:test:cljs-test-runner -v speculative.core-test/assoc-in-test

#### Self-hosted ClojureScript (Planck)

    clojure -A:test:cljs-test-runner -x planck -v speculative.core-test/assoc-in-test

Running `script/clean` before running tests is recommended, especially for
ClojureScript on Node. The script `script/test` automatically calls
`script/clean` and runs all tests for all environments.

### Coal-mine

[Coal-mine](https://github.com/mfikes/coal-mine) is a collection of 4clojure
solutions. These can be used to verify speculative specs.

Run a random coal-mine problem:

    script/coal-mine

Run a specific coal-mine problem:

    script/coal-mine --problem 77

Run a range of coal-mine problems:

    script/coal-mine --from 10 --to 15

Both `from` and `to` are inclusive.

Run with additional checks on `ret` and `fn` specs via
[orchestra](https://github.com/jeaye/orchestra) (EXPERIMENTAL):

    script/coal-mine --from 10 --to 15 --ret-spec true

To skip an environment (CLJ or CLJS):

    SKIP_CLJS=true script/coal-mine

## Try online

[KLIPSE REPL](https://re-find.it/speculative-repl) with speculative and
[expound](https://github.com/bhb/expound).

## Origins

The project started based on two tweets. First @mfikes tweeted

<blockquote class="twitter-tweet" data-lang="en"><p lang="en" dir="ltr">I still
hold the view that Clojure’s core fns should have specs. <br><br>Ex: While<br>
(merge-with + [0 3] {0 7 1 2} {0 3 2 32})<br>produces a reasonable result, it is
not even a map. A spec would reject 2nd arg.<br><br>What if I conclude dot
products are possible via<br> (merge-with + [0 3] [1 2])<br>?</p>&mdash; Mike
Fikes (@mfikes) <a
href="https://twitter.com/mfikes/status/1053304266239197184?ref_src=twsrc%5Etfw">October
19, 2018</a></blockquote>

Then @borkdude tweeted a couple of days later: <blockquote class="twitter-tweet"
data-conversation="none" data-lang="en"><p lang="en" dir="ltr">Or maybe have a
development version with guards and a production version without guards (I think
Stu said something like this)</p>&mdash; (λ. borkdude) (@borkdude) <a
href="https://twitter.com/borkdude/status/1053404362062606336?ref_src=twsrc%5Etfw">October
19, 2018</a></blockquote>

## Issues found

[These issues](doc/issues.md) were found while developing and using speculative.

## Users

[These projects](doc/users.md) are known to use speculative.

## Contributing

In case this code will ever be useful to `clojure.core`, any contributer to this
project needs to sign the [Contributor
Agreement](https://clojure.org/community/contributing) for Clojure so that any
code in speculative can be used in either Clojure or Clojurescript.

Please have look at the [contributor guidelines](CONTRIBUTING.md) before
submitting a PR.

## Contributors

(Generated by [Hall-Of-Fame](https://github.com/sourcerer-io/hall-of-fame))

[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/0)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/0)[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/1)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/1)[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/2)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/2)[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/3)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/3)[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/4)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/4)[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/5)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/5)[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/6)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/6)[![](https://sourcerer.io/fame/borkdude/borkdude/speculative/images/7)](https://sourcerer.io/fame/borkdude/borkdude/speculative/links/7)

## License

Copyright © 2018 Erik Assum, Michiel Borkent and Mike Fikes

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
