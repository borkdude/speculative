<img src="./logo/logo.svg">

[![CircleCI](https://circleci.com/gh/borkdude/speculative/tree/master.svg?style=svg)](https://circleci.com/gh/borkdude/speculative/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/speculative.svg)](https://clojars.org/speculative)
[![cljdoc badge](https://cljdoc.org/badge/speculative/speculative)](https://cljdoc.org/d/speculative/speculative/CURRENT)

speculative is a collection of specs for the functions in `clojure.core`. While
its ultimate goal is to be rendered obsolete by these or similar specs being
added to `clojure.core` proper, speculative hopefully provides some value while
we're waiting for that to happen.

## Rationale

With the new error-messages that are coming with Clojure 1.10, adding specs to
the `clojure.core` functions give much better error messages.

Without specs on `clojure.core/map` the error looks like:

```clojure
Clojure 1.10.0-RC1
user=> (map 'lol 'lol)
Error printing return value (IllegalArgumentException) at clojure.lang.RT.seqFrom (RT.java:551).
Don't know how to create ISeq from: clojure.lang.Symbol
user=>
```
With speculative, we get 

```
user=> (map 1 'lol)
Evaluation error - invalid arguments to clojure.core/map at (NO_SOURCE_FILE:4).
1 - failed: ifn? at: [:f]
user=>
```

## Installation

Add the relevant coordinates to your favourite build tool:

deps.edn

```
speculative {:mvn/version "0.0.2"}
```

lein

```
[speculative "0.0.2"]
```

## Usage

Speculative specs correspond to the namespaces in Clojure:

``` clojure
speculative.core -> clojure.core
speculative.set  -> clojure.set
...
```

To load all specs at once, you can require `speculative.instrument` which also
provides functions to only instrument speculative specs.

```clojure
$ clj
Clojure 1.10.0
user=> (require '[speculative.instrument :refer [instrument unstrument]])
nil
user=> (instrument)
[clojure.core/first clojure.core/apply clojure.core/assoc ...]

user=> (merge-with 1 {:a 2} {:a 3})
Execution error - invalid arguments to clojure.core/merge-with at (REPL:1).
1 - failed: ifn? at: [:f] spec: :speculative.specs/ifn

user=> (unstrument)
...
user=> (merge-with 1 {:a 2} {:a 3})
Execution error (ClassCastException) at user$eval344/invokeStatic (REPL:1).
java.lang.Long cannot be cast to clojure.lang.IFn
```

## Managing expectations

These specs try to be as accurate as possible, given what we know about the
newest releases of Clojure and ClojureScript. However, we cannot guarantee that
these specs are The Answer, once and for all. Our specs may be inaccurate and we
may change them based on new insights. Functions in future versions of Clojure
may allow different arguments and arities or return different values than we
account for at this time of writing. These specs have no official status, are
not endorsed by Cognitect and are provided without warranty.

## Speculative broke my project

Speculative specs find, when instrumented, incorrect or undefined usage of
Clojure core functions. If code is under your control, you can fix it. If the
call was made in a library not under your control, you can unstrument the spec
using `clojure.spec.test.alpha/unstrument`, unload it using `(s/def spec-symbol
nil)` or disable it within the scope of a body using
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

In the hope that the code in this project would be useful for `clojure.core`,
any contributer to this repo needs to have a [Contributor
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
