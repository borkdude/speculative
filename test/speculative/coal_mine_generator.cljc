(ns speculative.coal-mine-generator
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]))

(def syms
  '[coal-mine.problem-1
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
    coal-mine.problem-21
    coal-mine.problem-22
    coal-mine.problem-23
    coal-mine.problem-24
    coal-mine.problem-25
    coal-mine.problem-26
    coal-mine.problem-27
    coal-mine.problem-28
    coal-mine.problem-29
    coal-mine.problem-30
    coal-mine.problem-31
    coal-mine.problem-32
    coal-mine.problem-33
    coal-mine.problem-34
    coal-mine.problem-35
    coal-mine.problem-36
    coal-mine.problem-37
    coal-mine.problem-38
    coal-mine.problem-39
    coal-mine.problem-40
    coal-mine.problem-41
    coal-mine.problem-42
    coal-mine.problem-43
    coal-mine.problem-44
    coal-mine.problem-45
    coal-mine.problem-46
    coal-mine.problem-47
    coal-mine.problem-48
    coal-mine.problem-49
    coal-mine.problem-50
    coal-mine.problem-51
    coal-mine.problem-52
    coal-mine.problem-53
    coal-mine.problem-54
    coal-mine.problem-55
    coal-mine.problem-56
    coal-mine.problem-57
    coal-mine.problem-58
    coal-mine.problem-59
    coal-mine.problem-60
    coal-mine.problem-61
    coal-mine.problem-62
    coal-mine.problem-63
    coal-mine.problem-64
    coal-mine.problem-65
    coal-mine.problem-66
    coal-mine.problem-67
    coal-mine.problem-68
    coal-mine.problem-69
    coal-mine.problem-70
    coal-mine.problem-71
    coal-mine.problem-72
    coal-mine.problem-73
    coal-mine.problem-74
    coal-mine.problem-75
    coal-mine.problem-76
    coal-mine.problem-77
    coal-mine.problem-78
    coal-mine.problem-79
    coal-mine.problem-80
    coal-mine.problem-81
    coal-mine.problem-82
    coal-mine.problem-83
    coal-mine.problem-84
    coal-mine.problem-85
    coal-mine.problem-86
    coal-mine.problem-88
    coal-mine.problem-89
    coal-mine.problem-90
    coal-mine.problem-91
    coal-mine.problem-92
    coal-mine.problem-93
    coal-mine.problem-94
    coal-mine.problem-95
    coal-mine.problem-96
    coal-mine.problem-97
    coal-mine.problem-98
    coal-mine.problem-99
    coal-mine.problem-100
    coal-mine.problem-101
    coal-mine.problem-102
    coal-mine.problem-103
    coal-mine.problem-104
    coal-mine.problem-105
    coal-mine.problem-106
    coal-mine.problem-107
    coal-mine.problem-108
    coal-mine.problem-110
    coal-mine.problem-111
    coal-mine.problem-112
    coal-mine.problem-113
    coal-mine.problem-114
    coal-mine.problem-115
    coal-mine.problem-116
    coal-mine.problem-117
    coal-mine.problem-118
    coal-mine.problem-119
    coal-mine.problem-120
    coal-mine.problem-121
    coal-mine.problem-122
    coal-mine.problem-124
    coal-mine.problem-125
    coal-mine.problem-127
    coal-mine.problem-128
    coal-mine.problem-130
    coal-mine.problem-131
    coal-mine.problem-132
    coal-mine.problem-134
    coal-mine.problem-135
    coal-mine.problem-137
    coal-mine.problem-138
    coal-mine.problem-140
    coal-mine.problem-141
    coal-mine.problem-143
    coal-mine.problem-144
    coal-mine.problem-145
    coal-mine.problem-146
    coal-mine.problem-147
    coal-mine.problem-148
    coal-mine.problem-150
    coal-mine.problem-152
    coal-mine.problem-153
    coal-mine.problem-156
    coal-mine.problem-157
    coal-mine.problem-158
    coal-mine.problem-161
    coal-mine.problem-162
    coal-mine.problem-164
    coal-mine.problem-166
    coal-mine.problem-168
    coal-mine.problem-171])

(defn ns-form [problems]
  (let [require-syms problems]
    `(~'ns speculative.coal-mine-runner
       (:require
        ~@require-syms
        [speculative.core]
        [clojure.spec.test.alpha :as ~'stest]
        [clojure.test]))))

(defn run-tests-form [problems]
  (let [syms (map #(list 'quote %) problems)]
    `(defn ~'run-tests []
       (println "Running tests for" ~@syms)
       (clojure.test/run-tests ~@syms))))

(defn run-form []
  `(do
     (println "Instrumented fns:" (stest/instrument))
     (time (~'run-tests))
     ~(reader-conditional '(:clj (shutdown-agents)) false)))

(defn emit-program [out problems]
  (spit out (str (with-out-str (pprint (ns-form problems))) "\n"))
  (spit out (str (with-out-str (pprint (run-tests-form problems))) "\n")
        :append true)
  (spit out (str (with-out-str (pprint (run-form))) "\n")
        :append true))

(defn -main [& [out op max-or-nth]]
  (let [max-or-n #?(:clj (Integer/parseInt max-or-nth)
                    :cljs (js/parseInt max-or-nth))
        problems (case op
                   "nth"
                   [(nth syms max-or-n)]
                   "random"
                   (sort (take max-or-n (shuffle syms))))]
    (io/make-parents out)
    (emit-program out problems)))
