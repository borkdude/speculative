# speculative
[![CircleCI](https://circleci.com/gh/slipset/speculative/tree/master.svg?style=svg)](https://circleci.com/gh/slipset/speculative/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/speculative.svg)](https://clojars.org/speculative)

speculative is a collection of specs for the functions in `clojure.core`. While its ultimate goal is to be rendered obsolete by these or similar specs being added to `clojure.core` proper, speculative hopefully provides some value while we're waiting for that to happen.

# Origins

The project started based on two tweets. First @mfikes tweeted

<blockquote class="twitter-tweet" data-lang="en"><p lang="en" dir="ltr">I still hold the view that Clojure’s core fns should have specs. <br><br>Ex: While<br> (merge-with + [0 3] {0 7 1 2} {0 3 2 32})<br>produces a reasonable result, it is not even a map. A spec would reject 2nd arg.<br><br>What if I conclude dot products are possible via<br> (merge-with + [0 3] [1 2])<br>?</p>&mdash; Mike Fikes (@mfikes) <a href="https://twitter.com/mfikes/status/1053304266239197184?ref_src=twsrc%5Etfw">October 19, 2018</a></blockquote> 

Then @borkdude tweeted a couple of days later:
<blockquote class="twitter-tweet" data-conversation="none" data-lang="en"><p lang="en" dir="ltr">Or maybe have a development version with guards and a production version without guards (I think Stu said something like this)</p>&mdash; (λ. borkdude) (@borkdude) <a href="https://twitter.com/borkdude/status/1053404362062606336?ref_src=twsrc%5Etfw">October 19, 2018</a></blockquote>

## Rationale

With the new error-messages that are coming with Clojure 1.10, adding specs to the `clojure.core` functions give much better error messages.

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
speculative {:mvn/version "RELEASE"}
```

lein

```
[speculative "0.0.1"]
```

## Usage

```clojure
user=> (require 'speculative.core)
nil
user=> (require '[clojure.spec.test.alpha :as stest])
nil
user=> (stest/instrument `clojure.core/map)
[clojure.core/map]
user=> (map 1 'lol)
Evaluation error - invalid arguments to clojure.core/map at (NO_SOURCE_FILE:4).
1 - failed: ifn? at: [:f]
user=>

```

## Test tools

Namespace `speculative.test` provides macros and functions that are used in the
tests for speculative, but may also come in handy in other projects. You have to
bring in [macrovich](https://github.com/cgrand/macrovich) as an extra dependency
if you want to use this namespace.

``` clojure
$ clj -Sdeps '{:deps {net.cgrand/macrovich {:mvn/version "0.2.1"}}}'
Clojure 1.10.0-RC1

user=> (require '[speculative.test :refer [check
                                    with-instrumentation
                                    gentest
                                    success?]])
nil

user=> (require '[clojure.spec.alpha :as s])
nil

user=> (s/fdef foo
  :args (s/cat :n number?)
  :ret number?)
user/foo

user=> (defn foo [n]
  "ret")
#'user/foo

user=> (check `foo [1])
Evaluation error - invalid arguments to null at clojure.spec.test.alpha/explain-check (alpha.clj:278).
"ret" - failed: number? at: [:ret]

user=> (s/fdef foo
  :args (s/cat :n number?)
  :ret string?)
user/foo

user=> (check `foo [1])
"ret"

user=> (with-instrumentation `foo
  (foo "a"))
Evaluation error - invalid arguments to user/foo at (NO_SOURCE_FILE:15).
"a" - failed: number? at: [:n]

user=> (foo "a")
"ret"

user=> (gentest `foo nil {:num-tests 1})
generatively testing user/foo
({:spec #object[clojure.spec.alpha$fspec_impl$reify__2524 0x72bd06ca "clojure.spec.alpha$fspec_impl$reify__2524@72bd06ca"], :clojure.spec.test.check/ret {:result true, :pass? true, :num-tests 1, :time-elapsed-ms 1, :seed 1541249961647}, :sym user/foo})

user=> (success? *1)
true

user=>
```

## Issues detected by usage of speculative

[These issues](doc/issues.md) were detected by usage of speculative.

## Tests

### Clojure

     clj -A:test:clj-tests
     
### ClojureScript

    clj -A:test:cljs-tests
    
### Self-Hosted ClojureScript
   
    plk -A:test:plk-tests
    
## Contributing

In the hope that the code in this project would be useful for `clojure.core`, any contributer to this repo needs to have a 
[Contributor Agreement](https://clojure.org/community/contributing) for Clojure so that any code in speculative can be used in either Clojure or Clojurescript.

Take a look at the [style guide](doc/style.md).

## License

Copyright © 2018 Erik Assum

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
