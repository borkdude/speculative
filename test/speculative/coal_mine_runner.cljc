(ns speculative.coal-mine-runner
  (:require
   coal-mine.problem-1
   coal-mine.problem-2
   coal-mine.problem-3
   coal-mine.problem-4
   coal-mine.problem-5
   coal-mine.problem-6
   coal-mine.problem-7
   coal-mine.problem-8
   coal-mine.problem-9
   coal-mine.problem-10
   coal-mine.problem-11
   coal-mine.problem-12
   coal-mine.problem-13
   coal-mine.problem-14
   coal-mine.problem-15
   coal-mine.problem-16
   coal-mine.problem-17
   coal-mine.problem-18
   coal-mine.problem-19
   coal-mine.problem-20
   ;; coal-mine.problem-21
   ;; coal-mine.problem-22
   ;; coal-mine.problem-23
   ;; coal-mine.problem-24
   ;; coal-mine.problem-25
   ;; coal-mine.problem-26
   ;; coal-mine.problem-27
   ;; coal-mine.problem-28
   ;; coal-mine.problem-29
   ;; coal-mine.problem-30
   ;; coal-mine.problem-31
   ;; coal-mine.problem-32
   ;; coal-mine.problem-33
   ;; coal-mine.problem-34
   ;; coal-mine.problem-35
   ;; coal-mine.problem-36
   ;; coal-mine.problem-37
   ;; coal-mine.problem-38
   ;; coal-mine.problem-39
   ;; coal-mine.problem-40
   ;; coal-mine.problem-41
   ;; coal-mine.problem-42
   ;; coal-mine.problem-43
   ;; coal-mine.problem-44
   ;; coal-mine.problem-45
   ;; coal-mine.problem-46
   ;; coal-mine.problem-47
   ;; coal-mine.problem-48
   ;; coal-mine.problem-49
   ;; coal-mine.problem-50
   [respeced.test :refer [planck-env?]]
   [clojure.test]
   #?(:clj patch.clj-2443))
  #?(:cljs (:require-macros [speculative.coal-mine-runner :refer [run-tests]])))

#?(:clj
   (defmacro run-tests [n]
     (let [syms (map #(list 'quote (symbol
                                    (str "coal-mine.problem-" %)))
                     (range 1 (inc n)))]
       `(clojure.test/run-tests ~@syms))))

(defn -main [& args]
  (time (run-tests 20))
  #?(:clj (shutdown-agents)))

#?(:cljs (set! *main-cli-fn* -main))
