(ns speculative.coal-mine-generator
  (:require
   [clojure.java.io :as io]
   [coal-mine.script :as cm]))

(def problem-numbers (set cm/problem-numbers))

(defn ns-form [problem]
  (format "(ns speculative.coal-mine-runner
(:require %s
#?(:clj patch.clj-2443)
[speculative.instrument :refer [instrument]]
[clojure.test])
)" problem))

(defn run-tests-form [problem]
  (format "(defn run-tests []
(println \"Instrumenting with speculative specs\")
(instrument)
(println \"Running tests %s\")
(clojure.test/run-tests '%s))"
          problem problem))

(defn run-form []
  "(time (run-tests))
#?(:clj (shutdown-agents))")

(defn emit-program [out problem]
  (spit out (str (ns-form problem) "\n"))
  (spit out (str (run-tests-form problem) "\n")
        :append true)
  (spit out (str (run-form) "\n")
        :append true))

(defn -main [& [out problem]]
  (let [problem (or (and problem (Integer/parseInt problem))
                    (rand-nth (seq problem-numbers)))
        problem (if (contains? problem-numbers problem)
                  (str "coal-mine.problem-" problem)
                  (throw (Exception. (str "problem " problem " does not exist"))))]
    (io/make-parents out)
    (emit-program out problem)))
