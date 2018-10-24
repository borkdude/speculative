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

## Tests

### Clojure

     clj -A:test:cljtests
     
### ClojureScript

    clj -A:test:cljstests
    
### Self-Hosted ClojureScript
   
    plk -A:test:plktests
    
## Contributing

In the hope that the code in this project would be useful for `clojure.core`, any contributer to this repo needs to have a 
[Contributor Agreement](https://clojure.org/community/contributing) for Clojure so that any code in speculative can be used in either Clojure or Clojurescript.

## License

Copyright © 2018 Erik Assum

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
