;; Maybe use this for string from regex generation
(ns string-gen
  (:require [com.wsscode.test.chuck.charsets :as charsets]
            [clojure.test.check.generators :as gen]))

(defn charset->gen [charset]
  (let [size (charsets/size charset)]
    (if (zero? size)
      (throw (ex-info "Cannot generate characters from empty class!"
               {:type ::ungeneratable}))
      (gen/fmap (partial charsets/nth charset)
        (gen/choose 0 (dec size))))))

(def type->charset
  {"D" (charsets/range "0" "9")
   "W" (charsets/union (charsets/range "0" "9") (charsets/range "a" "z") (charsets/range "A" "Z"))
   "A" (charsets/range "A" "Z")})

(defn parse-int [x]
  (js/parseInt x))

(defn token->gen [token]
  (cond
    (string? token)
    (gen/return token)

    (keyword? token)
    (if-let [[_ t n] (re-find #"([DAW])(\d+)" (name token))]
      (gen/fmap #(apply str %)
        (gen/vector (charset->gen (type->charset t)) (parse-int n)))
      (throw (ex-info "Invalid keyword token" {:token token})))

    :else
    (throw (ex-info "Invalid token" {:token token}))))

(defn string-gen [tokens]
  (->> tokens
       (mapv token->gen)
       (apply gen/tuple)
       (gen/fmap #(apply str %))))
