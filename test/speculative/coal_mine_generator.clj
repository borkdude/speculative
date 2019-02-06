(ns speculative.coal-mine-generator
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [coal-mine.script :as cm]))

(def problem-numbers (set cm/problem-numbers))

(defn ns-form [problems ret-specs?]
  (format "(ns speculative.coal-mine-runner
(:require %s
#?(:clj patch.clj-2443)
%s
[clojure.spec.alpha :as s]
[speculative.instrument :refer [instrument]]
[clojure.test])
)" (str/join " " problems)
          (if ret-specs?
            "#?(:clj [orchestra.spec.test :as stest]
                :cljs [orchestra-cljs.spec.test :as stest])"
            "[clojure.spec.test.alpha :as stest]")))

(defn run-tests-form [problems]
  (format "(defn run-tests []
(println \"Instrumenting with speculative specs\")
(stest/instrument)
(println \"Running tests %s\")
(clojure.test/run-tests %s))"
          (str/join ", " problems)
          (str/join " " (map #(str "'" %) problems))))

(defn run-form []
  "(time (run-tests))
#?(:clj (shutdown-agents))")

(defn emit-program [out problems ret-specs?]
  (spit out (str (ns-form problems ret-specs?) "\n"))
  (spit out (str (run-tests-form problems) "\n")
        :append true)
  (spit out (str (run-form) "\n")
        :append true))

(def cli-options
  [["-p" "--problem PROBLEM" "problem"
    :parse-fn #(Integer/parseInt %)]
   ["-f" "--from FROM" "from problem"
    :parse-fn #(Integer/parseInt %)]
   ["-t" "--to TO" "to problem (inclusive)"
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--ret-specs" "check ret and fn specs with Orchestra"]
   ["-o" "--out OUT" "file to write output to" :default "out/out.cljc"]])

(defn -main [& args]
  (let [{:keys [:from :to :problem
                :ret-specs :out] :as o} (:options (parse-opts args cli-options))
        [from to] (cond problem [problem problem]
                        (and from to) [from to]
                        :else (let [random (rand-nth (seq problem-numbers))]
                                [random random]))
        problem-range (range from (inc to))
        problems (keep problem-numbers (range from (inc to)))
        problems (if (seq problems)
                   (mapv #(str "coal-mine.problem-" %) problems)
                   (throw (Exception. (str "no problems found in range " (vec problem-range)))))]
    (io/make-parents out)
    (emit-program out problems ret-specs)))

;; NOTE: problem 101 takes an insane amount of time to run
