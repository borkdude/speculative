# speculative
[![CircleCI](https://circleci.com/gh/slipset/speculative/tree/master.svg?style=svg)](https://circleci.com/gh/slipset/speculative/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/speculative.svg)](https://clojars.org/speculative)

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
Clojure 1.10.0-beta6
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

## Speculative broke my project

Speculative specs find, when instrumented, incorrect or undefined usage of
Clojure core functions. If code is under your control, you can fix it. If the
call was made in a library not under your control, you can unstrument the spec
using `clojure.spec.test.alpha/unstrument` or disable it within the scope of a
body using `speculative.test/with-unstrumentation`:

``` clojure
$ clj
Clojure 1.10.0-beta6
user=> (require '[speculative.test :refer [with-unstrumentation]])
nil
user=> (require '[speculative.instrument :refer [instrument]])
nil
user=> (instrument)
[clojure.core/first clojure.core/apply clojure.core/assoc ...]
user=> (merge #{1 2 3} 4)
Execution error - invalid arguments to clojure.core/merge at (REPL:1).
#{1 3 2} - failed: map? at: [:maps :init-map :clojure.spec.alpha/pred]
#{1 3 2} - failed: nil? at: [:maps :init-map :clojure.spec.alpha/nil]
user=> (speculative.test/with-unstrumentation `merge (merge #{1 2 3} 4))
#{1 4 3 2}
```

If you believe the spec was wrong, please create an
[issue](https://github.com/slipset/speculative/issues).


## Test tools

Namespace `speculative.test` provides various tools around
`clojure.spec.test.alpha`. More info [here](doc/test.md).

## Tests

### Clojure

    clj -A:test:clj-tests
     
### ClojureScript

    script/cljs-tests
    
### Self-Hosted ClojureScript
   
    plk -A:test:plk-tests

## Try online

[KLIPSE REPL](http://bit.ly/speculative-repl) with speculative and
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

Please have look at the [style guide](doc/style.md) before submitting a PR.

## License

Copyright © 2018 Erik Assum

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
