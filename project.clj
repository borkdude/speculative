(defproject speculative "0.0.3-SNAPSHOT"
  :description "A collection of specs for clojure.core functions"
  :url "https://github.com/borkdude/speculative"
  :scm {:name "git"
        :url "https://github.com/borkdude/speculative"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :profiles {:dev {:dependencies
                   [[org.clojure/clojurescript "1.10.439"]
                    [org.clojure/test.check "0.9.0"]]}}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                  :username :env/clojars_user
                                  :password :env/clojars_pass
                                  :sign-releases false}]])
