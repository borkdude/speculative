## Test tools

Namespace `speculative.test` provides macros and functions that are used in the
tests for speculative, but may also come in handy in other projects.

``` clojure
$ clj -Sdeps '{:deps {net.cgrand/macrovich {:mvn/version "0.2.1"}}}'
Clojure 1.10.0-RC1

user=> (require '[speculative.test :refer [check
                                    with-instrumentation
                                    gentest
                                    successful?]])
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

user=> (successful? *1)
true

user=>
```
