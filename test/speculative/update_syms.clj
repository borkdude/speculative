(ns speculative.update-syms
  (:require
   [clojure.set :as set]
   [clojure.spec.test.alpha :as stest]
   [clojure.string :as str]
   [speculative.core]
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
(def blacklist `[not some? str = get])
(def blacklist-clj blacklist)
(def blacklist-cljs (cljsify (into blacklist `[next str apply =])))
(def instrumentable-syms-clj (set/difference all-syms-clj blacklist-clj))
(def instrumentable-syms-cljs (set/difference all-syms-cljs blacklist-cljs))

(defn -main [& args]
  (spit "src/speculative/impl/syms.cljc" (format template
                                                 all-syms-clj
                                                 all-syms-cljs
                                                 blacklist-clj
                                                 blacklist-cljs
                                                 instrumentable-syms-clj
                                                 instrumentable-syms-cljs))
  (println "Update instrumentable-sym-counts in test/speculative/update_syms.clj with"
           (zipmap [:clj :cljs]
                   (map count [instrumentable-syms-clj
                               instrumentable-syms-cljs]))))
