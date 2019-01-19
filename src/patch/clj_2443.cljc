(ns patch.clj-2443
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]))

(in-ns 'clojure.spec.test.alpha)

(defn- spec-checking-fn
  [v f fn-spec]
  (let [fn-spec (@#'s/maybe-spec fn-spec)
        args-spec (:args fn-spec)
        conform! (fn [v role spec data args]
                   (when args-spec
                     (let [conformed (s/conform spec data)]
                       (if (= ::s/invalid conformed)
                         (let [caller (->> (.getStackTrace (Thread/currentThread))
                                           stacktrace-relevant-to-instrument
                                           first)
                               ed (merge (assoc (s/explain-data* spec [] [] [] data)
                                                ::s/fn (->sym v)
                                                ::s/args args
                                                ::s/failure :instrument)
                                         (when caller
                                           {::caller (dissoc caller :class :method)}))]
                           (throw (ex-info
                                   (str "Call to " v " did not conform to spec.")
                                   ed)))
                         conformed))))]
    (fn
     [& args]
      (if *instrument-enabled*
        (let [pre-conformed?
              (and (instance? clojure.lang.Cons args)
                   (do (conform! v :args args-spec args args)
                       true))]
           (with-instrument-disabled
             (when-not pre-conformed?
               (conform! v :args args-spec args args))
             (binding [*instrument-enabled* true]
               (.applyTo ^clojure.lang.IFn f args))))
       (.applyTo ^clojure.lang.IFn f args)))))

(in-ns 'patch.clj-2443)

(comment
  (do
    (require '[clojure.test :refer [deftest is run-tests]])
    
    (deftest clj-2443-test
      (defn map-f [x]
        {x 1})

      (s/fdef map-f :args (s/cat :x symbol?))

      (defn varargs-f [& maps]
        true)

      (s/fdef varargs-f :args (s/cat :maps (s/* map?)))

      (defn clj-2443 [& args]
        (apply varargs-f (map map-f args)))

      (stest/instrument)

      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Call to #'patch.clj-2443/map-f did not conform to spec."
           (clj-2443 'foo 'bar "baz"))))

    (run-tests)))
