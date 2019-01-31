(ns speculative.coal-mine-generator
  (:require
   [clojure.java.io :as io]
   [coal-mine.script :as cm]
   [clojure.string :as str]))

(def problem-numbers (set cm/problem-numbers))

(defn ns-form [problems]
  (format "(ns speculative.coal-mine-runner
(:require %s
#?(:clj patch.clj-2443)
[speculative.instrument :refer [instrument]]
[clojure.test])
)" (str/join " " problems)))

(defn run-tests-form [problems]
  (format "(defn run-tests []
(println \"Instrumenting with speculative specs\")
(instrument)
(println \"Running tests %s\")
(clojure.test/run-tests %s))"
          (str/join ", " problems)
          (str/join " " (map #(str "'" %) problems))))

(defn run-form []
  "(time (run-tests))
#?(:clj (shutdown-agents))")

(defn emit-program [out problems]
  (spit out (str (ns-form problems) "\n"))
  (spit out (str (run-tests-form problems) "\n")
        :append true)
  (spit out (str (run-form) "\n")
        :append true))

(defn -main [& [out problem-from problem-to]]
  (let [[from to] [(when problem-from
                     (Integer/parseInt problem-from))
                   (when problem-to
                     (Integer/parseInt problem-to))]
        [from to] (if from [from (or to from)]
                      (let [random (rand-nth (seq problem-numbers))]
                        [random random]))
        problem-range (range from (inc to))
        problems (keep problem-numbers (range from (inc to)))
        problems (if (seq problems)
                   (mapv #(str "coal-mine.problem-" %) problems)
                   (throw (Exception. (str "no problems found in range " (vec problem-range)))))]
    (io/make-parents out)
    (emit-program out problems)))
