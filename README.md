# speculative
[![CircleCI](https://circleci.com/gh/slipset/speculative/tree/master.svg?style=svg)](https://circleci.com/gh/slipset/speculative/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/speculative.svg)](https://clojars.org/speculative)

speculative is a collection of specs for the functions in `clojure.core`. While its ultimate goal is to be rendered obsolete by these or similar specs being added to `clojure.core` proper, speculative hopefully provides some value while we're waiting for that to happen.

## Rationale

With the new error-messages that are coming with Clojure 1.10, adding specs to the `clojure.core` functions give much better error messages.

Withoug specs on `clojure.core/map` the error looks like:

```clojure
Clojure 1.10.0-RC1
user=> (map 'lol 'lol)
Error printing return value (IllegalArgumentException) at clojure.lang.RT.seqFrom (RT.java:551).
Don't know how to create ISeq from: clojure.lang.Symbol
(user=>
```

With speculative, we get 

```clojure
user=> (map 'lol 'lol)
Evaluation error - invalid arguments to clojure.core/map at (NO_SOURCE_FILE:4).
lol - failed: fn? at: [:f]
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
user=> (map 'lol 'lol)
Evaluation error - invalid arguments to clojure.core/map at (NO_SOURCE_FILE:4).
lol - failed: fn? at: [:f]
user=>

```

## Tests

### Clojure

     clj -A:test:runner
     
### ClojureScript

    plk -A:test -e "(require '[clojure.test :as t])" -e "(require '[speculative.core-test])" -e "(t/run-tests 'speculative.core-test)"

## License

Copyright © 2018 Erik Assum

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
