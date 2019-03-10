(ns speculative.clojuredocs
  (:require
   [cheshire.core]
   [clojail.core :as clojail]
   [clojail.jvm :refer [permissions domain context jvm-sandbox]]
   [clojail.testers :as ct]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [finitize.core :refer [finitize]]
   [speculative.instrument :as i])
  (:import [java.io StringReader]))

;; to reproduce the CSV file:
;; download https://cl.ly/0R1M0Z0n1F3Y/clojuredocs-clean-db-backup-20180120.tar.gz and unzip
;; cd clojuredocs
;; install mongodb and run: mongod --dbpath .
;; run: mongorestore -d clojuredocs --drop .
;; run: mongoexport --db clojuredocs --collection examples --type csv --fields body,var --out examples.csv

(def ns-blacklist
  '#{clojure.java.io
     clojure.java.browse
     clojure.java.javadoc
     clojure.java.shell
     clojure.template
     clojure.test})

(def var-blacklist
  '#{clojure.core/*1
     clojure.core/*2
     clojure.core/*3
     clojure.core/read-line
     clojure.core/spit
     clojure.core/slurp
     clojure.core/with-open
     clojure.core/ns
     clojure.core/create-ns
     clojure.core/ns-unmap
     clojure.core/refer-clojure
     clojure.core/*command-line-args*
     clojure.core/*warn-on-reflection*
     clojure.core/read-string
     clojure.core/*print-length*
     clojure.core/defn-
     clojure.core/var
     clojure.core/require
     clojure.core/resolve
     clojure.core/load-file
     clojure.core/*print-dup*
     clojure.core/eval
     clojure.core/load
     clojure.core/binding
     clojure.core/*ns*
     clojure.core/*read-eval*
     clojure.core/find-var
     clojure.core/future-cancelled?
     clojure.core/future-done?
     clojure.core/gen-class
     clojure.core/import
     clojure.core/io!
     clojure.core/load-reader
     clojure.core/await
     clojure.core/agent})

(def expression-blacklist
  '#{(keyword? x)
     (the-ns my-namespace)
     (the-ns (quote for-later-use))
     (== 1 "1")
     (== 1 \1)
     (time (doall (...)))
     (twice 4)
     (six-times 100)
     v.1.3.0
     v.1.6.0
     (prn y)
     (prompt "How old are you?")
     (shutdown-agents)
     (assoc vec index replacement)
     (pvalues (expensive-calc-1) (expensive-calc-2))
     (var user/users)
     (var user/p)
     (var user/s)
     (var user/s1)
     (var user/s2)
     (var user/foo)
     (var user/countif)
     (var user/has-value)
     (nil nil nil)
     (ns foo)
     ("one" ", " "two" ", " "three")
     (type fn)
     (type clojure.core/fn)
     (subs "Clojure" 1 20)
     (quote)
     (quote 1 2 3 4 5)
     (because they are happening in different threads)
     (format "%5d" 12345678901234567890)
     (format "%.3f" 2)
     (nth (sieve (iterate inc 2)) 10000)})

(def examples-blacklist
  #{97 99 118 120 134 188 209 213 221 309 315 347 355 372
    402 406 415 420 423 432 434 435 445 449 450 457 474
    499 517 531 538 568 581 582 593 597 689 694 696 707
    708 710 712 715
    730 ;; somehow this examples take long in the sandbox,
    ;; but completes subsecond outside of it
    734 748 763 771 786})

(def catch
  '#{(tos ll)
     (* 3037000500 3037000500)
     (let [num (* 1234567890 21)] [num (int num) (long num)])
     (into-array [2 "4" "8" 5])
     (* 1234567890 9876543210)
     (zero? nil)
     (re-groups matcher)
     (/)
     (/ 1 0)
     (/ 0)
     (assoc [1 2 3] 4 10)
     (read-string "1.1.1 (+ 1 1)")
     (float Double/MAX_VALUE)
     (assert false)
     (assert nil)
     (dosync (ref-set (ref 1 :validator pos?) 0))
     (ref 0 :validator pos?)
     (aget a 2 0)
     (aget a 0 2)
     (name nil)
     (name 2)
     (condp some [1 2 3 4] #{0 7 6} :>> inc #{9 5} :>> dec)
     (quot 15 0)
     (println (tos (circle [2 3] 3.3)))
     (println (tos (line [1 1] [0 0])))
     (let [mystr "no match"] (case mystr "" 0 "hello" (count mystr)))
     (test #'my-function)
     (conj! foo 4)
     (persistent! foo)
     (contains? '(1 2 3) 1)
     (greeting spanish-map)
     (int "1")
     (nfirst #{1 3 2})
     (my-order)
     (nth [] 0)
     (subseq [1 2 3 4] > 2)
     (trim nil)
     (trim 1.1)
     (trim [1 2 3])
     (apply add2 [4 {:plus 1}])
     (reduce * (repeat 20 1000))
     (set! *warn-on-reflection* true)
     (keyword "user" 'abc)
     (keyword *ns* "abc")
     (keyword 'user "abc")
     (max [1 2 3])
     (flatten nil)
     (flatten 5)
     (flatten {:name "Hubert" :age 23})})

(defn repl-symbol? [s]
  (contains? '#{user> user=>
                demo.ns=>
                => users=>
                test=>} s))

(defn skip-rest-of-line
  "Reads until next newline and returns what is read. Returns `nil` if
  EOF is already reached."
  [in]
  (let [nl (int (first (with-out-str (newline))))]
    (loop [next-char (.read in)
           skipped nil]
      (cond (= -1 next-char) skipped
            (= nl next-char) (or skipped "")
            :else (recur (.read in)
                         (str skipped (char next-char)))))))

(defn try-read
  [in eof]
  (try (let [expr (binding [*read-eval* false] (read in false eof))]
         (cond (contains? expression-blacklist expr) nil
               (contains? catch expr)
               (do (println "catches" expr)
                   `(try ~expr (catch Throwable e# nil)))
               :else expr))
       (catch Throwable e
         (println "could not read" (.getMessage e))
         (skip-rest-of-line in))))

(defn unparse [pbr s]
  (.unread pbr (.toCharArray (str s))))

(defn parse-expr-after-repl-symbol
  [current-repl-symbol in eof]
  (let [expr (try-read in eof)]
    (cond (= eof expr) nil
          (repl-symbol? expr)
          (recur current-repl-symbol in eof)
          :else
          (let [post-expr (try-read in eof)]
            (if (= current-repl-symbol post-expr) #_(repl-symbol? post-expr)
                (do (unparse in post-expr)
                    expr)
                (do
                  (when (and (symbol? post-expr) (str/ends-with? (str post-expr) "Exception"))
                    (skip-rest-of-line in))
                  expr))))))

(defn parse-repl-session
  [in eof]
  (loop [exprs []]
    (let [expr (try-read in eof)]
      (cond
        (= eof expr) exprs
        (repl-symbol? expr)
        (do
          (let [next-expr (parse-expr-after-repl-symbol expr in eof)]
            (recur (conj exprs next-expr))))
        :else (recur (conj exprs expr))))))

(defn function-call? [expr]
  (and (seq? expr)
       (let [f (first expr)]
         (and (symbol? f)
              (not= 'var f)))))

(defn read-expressions
  [example-string]
  (let [eof (Object.)
        in (java.io.PushbackReader. (StringReader. example-string) 100)
        exprs (with-open [in in]
                (loop [exprs []]
                  (let [next-expr (try-read in eof)]
                    (cond
                      (= eof next-expr) exprs
                      (repl-symbol? next-expr)
                      (do
                        (unparse in next-expr)
                        (into exprs (parse-repl-session in eof)))
                      (nil? next-expr) (recur exprs)
                      :else (recur (conj exprs next-expr))))))]
    (filter function-call? exprs)))

(defn split-sessions
  ;; people tend to split their REPL session + output by a blank line
  ;; we add \S to not match blank lines within s-exprs
  [s]
  (let [with-divider (str/replace s #"\n\n(\S)"
                                  (fn [[_ non-whitespace]]
                                    (str "\n\n#_DIVIDER " non-whitespace)))]
    (str/split with-divider #"#_DIVIDER")))

(defn process-example
  [{:keys [:ns :var :body :n] :as raw-example}]
  (let [code (when-not (or
                        (contains? ns-blacklist ns)
                        (contains? var-blacklist var)
                        (contains? examples-blacklist n))
               (-> body
                   (str/replace "<pre>" "")
                   (str/replace "</pre>" "")
                   (str/replace "user=&gt;" "user=>")
                   (str/replace "clojure.contrib.math" "clojure.math.numeric-tower")
                   (str/replace "account-level" "identity")
                   (str/replace "(java.io.File. \"tryout.mp3\")" "(clojure.java.io/reader \"http://www.largesound.com/ashborytour/sound/brobob.mp3\")")
                   (str/replace #"(?m)^->" "#_")
                   (str/replace "(Thread/sleep 10000)" "(Thread/sleep 100)")
                   (str/replace "(Thread/sleep 5000)" "(Thread/sleep 100)")
                   (str/replace "(Thread/sleep 400)" "(Thread/sleep 100)")
                   (str/replace "(Thread/sleep 3000)" "(Thread/sleep 100)")))
        splitted-code (when code (split-sessions code))
        exprs (mapcat read-expressions splitted-code)
        [toplevel-exprs in-fn-exprs]
        (let [g (group-by
                 #(and (list? %)
                       (contains? '#{clojure.core/use use
                                     require import defprotocol
                                     defrecord}
                                  (first %)))
                 exprs)]
          [(get g true) (get g false)])]
    (merge raw-example
           {:toplevel toplevel-exprs
            :sandboxed in-fn-exprs})))

(def speculative-tester
  [(ct/blacklist-objects [clojure.lang.Compiler clojure.lang.Ref clojure.lang.Reflector
                          clojure.lang.RT
                          java.io.ObjectInputStream])
   (ct/blacklist-packages ["java.lang.reflect"
                           "java.security"
                           "java.awt"])
   (ct/blacklist-symbols
    '#{eval load-string load-reader addMethod ns-resolve resolve find-var
       *read-eval* ns-unmap ns-map
       System/out System/in System/err
       Class/forName})
   (ct/blacklist-nses '[clojure.main])
   (ct/blanket "clojail")])

(defn execute-example [example]
  (let [{:keys [:toplevel :var :sandboxed :ns]}
        example
        _ (prn "VAR" var)
        init `(do (require 'clojure.math.numeric-tower)
                  (import 'java.util.Date)
                  (use 'clojure.pprint)
                  ~@toplevel)
        sb (clojail/sandbox speculative-tester
                            :timeout 5000
                            :init init
                            :context (-> (permissions
                                          (java.util.PropertyPermission. "line.separator" "read"))
                                         domain
                                         context)
                            :transform finitize)]
    (sb '(use 'clojure.repl))
    (sb '(use 'clojure.pprint))
    (if (= ns 'clojure.set)
      (sb '(require '[clojure.set :refer :all]))
      (sb '(require '[clojure.set :refer :all :rename {join set-join}])))
    (doall
     (loop [sandboxed sandboxed
            repl-1 nil
            results []]
       (if-let [expr (first sandboxed)]
         (let [res (try
                     (prn "executing" expr)
                     (sb expr {#'*1 repl-1})
                     (catch SecurityException e
                       (prn "could not execute expression" expr (type e))
                       (throw e))
                     (catch java.util.concurrent.ExecutionException e
                       (prn "could not execute expression" expr (type e))
                       (throw e))
                     (catch clojure.lang.ExceptionInfo e
                       (try (throw (.getCause e))
                            (catch clojure.lang.Compiler$CompilerException e nil))))]
           (recur (rest sandboxed)
                  res
                  (conj results res)))
         results)))))

(defn load-raw-examples [csv-file]
  (map-indexed (fn [n [body var]]
                 (let [{:keys [:ns :name]} (cheshire.core/parse-string var true)
                       var (symbol ns name)
                       ns (symbol ns)]
                   {:n n
                    :ns ns
                    :name name
                    :body body
                    :var var}))
               (rest
                (doall
                 (csv/read-csv
                  (slurp csv-file))))))

(def cli-options
  [["-c" "--csv CSV" "CSV export from ClojureDocs"
    :default "https://michielborkent.nl/speculative/clojuredocs-20180120.csv"]
   ["-s" "--start START" "Start of example range"
    :default 0
    :parse-fn #(Integer/parseInt %)]
   ["-e" "--end END" "End of example range"
    :default 800
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--random RANDOM" "Run n random examples"
    :parse-fn #(Integer/parseInt %)]
   ["-v" "--var VAR" "Run examples by var"
    :parse-fn (fn [v]
                (let [[ns name] (str/split v #"/")]
                  (symbol ns name)))]])

(defn -main [& args]
  (i/instrument)
  (let [{:keys [:start :end :csv :random :var]} (:options (parse-opts args cli-options))
        raw-examples (vec (load-raw-examples csv))
        selection (cond
                    random (take random (shuffle (range 0 800)))
                    var (keep #(when (= var (:var %))
                                 (:n %)) raw-examples)
                    (and start end)
                    (range start end))
        raw-examples (map #(nth raw-examples %) selection)
        examples (map process-example raw-examples)]
    (doseq [e examples]
      (println "==== executing example" (:n e) "====")
      (execute-example e))))

;;;; Scratch

(comment
  (-main "-r" "2")
  (i/instrument)
  (def raw-examples
    (load-raw-examples "https://michielborkent.nl/speculative/clojuredocs-20180120.csv"))
  (process-example (nth raw-examples 414))
  )
