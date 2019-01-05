(ns speculative.update-syms
  (:require
   [clojure.set :as set]
   [clojure.spec.test.alpha :as stest]
   [clojure.string :as str]
   [speculative.core]
   [speculative.impl.syms :as prev-syms]
   [speculative.set]
   [speculative.string]))

(def template "(ns speculative.impl.syms
  (:require [clojure.set] [clojure.string]))

  (def all-syms-clj '%s)
  (def all-syms-cljs '%s)
  (def blacklist-clj '%s)
  (def blacklist-cljs '%s)
  (def instrumentable-syms-clj '%s)
  (def instrumentable-syms-cljs '%s)
  ")

(defn cljsify [syms]
  (set
   (map
    (fn [sym]
      (let [ns (namespace sym)
            ns (str/replace ns #"^clojure\.core" "cljs.core")
            sym (symbol ns (name sym))]
        sym))
    syms)))

;; Symbols on blacklist have no point of being instrumented, since there is
;; almost no way to call them with wrong arguments, or they are not
;; instrumentable for the enviroment.

(def all-syms (stest/instrumentable-syms))
(def all-syms-clj all-syms)
(def all-syms-cljs (cljsify (disj all-syms `re-matcher `re-groups)))
(def blacklist (set `[not some? str = get]))
(def blacklist-clj blacklist)
(def blacklist-cljs (cljsify (into blacklist `[next str apply =])))
(def instrumentable-syms-clj (set/difference all-syms-clj blacklist-clj))
(def instrumentable-syms-cljs (set/difference all-syms-cljs blacklist-cljs))

(defn -main [& args]
  (println "==== Changes")
  (println "all-syms-clj"
           "added"
           (set/difference all-syms-clj prev-syms/all-syms-clj)
           "removed"
           (set/difference prev-syms/all-syms-clj all-syms-clj))
  (println "all-syms-cljs"
           "added"
           (set/difference all-syms-cljs prev-syms/all-syms-cljs)
           "removed"
           (set/difference prev-syms/all-syms-cljs all-syms-cljs))
  (println "blacklist-clj"
           "added"
           (set/difference blacklist-clj prev-syms/blacklist-clj)
           "removed"
           (set/difference prev-syms/blacklist-clj blacklist-clj))
  (println "blacklist-cljs"
           "added"
           (set/difference blacklist-cljs prev-syms/blacklist-cljs)
           "removed"
           (set/difference prev-syms/blacklist-cljs blacklist-cljs))
  (println "instrumentable-syms-clj"
           "added"
           (set/difference instrumentable-syms-clj prev-syms/instrumentable-syms-clj)
           "removed"
           (set/difference prev-syms/instrumentable-syms-clj instrumentable-syms-clj))
  (println "instrumentable-syms-cljs"
           "added"
           (set/difference instrumentable-syms-cljs prev-syms/instrumentable-syms-cljs)
           "removed"
           (set/difference prev-syms/instrumentable-syms-cljs instrumentable-syms-cljs))
  (println "=====")
  (println "Update src/speculative/impl/syms.cljc with changes? Y/n")
  (let [s (read-line)]
    (if (= "Y" s)
      (do
        (spit "src/speculative/impl/syms.cljc" (format template
                                                       all-syms-clj
                                                       all-syms-cljs
                                                       blacklist-clj
                                                       blacklist-cljs
                                                       instrumentable-syms-clj
                                                       instrumentable-syms-cljs))
        (println "Succesfully wrote file."))
      (println "Not updated file."))))
