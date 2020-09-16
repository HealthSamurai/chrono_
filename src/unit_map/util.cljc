(ns unit-map.util
  (:require [clojure.string :as str])
  (:refer-clojure :exclude [infinite?]))


(defn NaN?
  "Test if this number is nan
   Nan is the only value for which equality is false"
  [x]
  (false? (== x x)))


(defn sanitize [s]
  (str/replace s #"[-.\+*?\[^\]$(){}=!<>|:\\]" #(str \\ %)))


(defn parse-int [x]
  (when (string? x)
    #?(:clj (try (Integer/parseInt x) (catch NumberFormatException e nil))
       :cljs (let [x* (js/parseInt x)] (when-not (NaN? x*) x*)))))


(defn pad-str [p n s]
  (->> (concat (reverse s) (repeat p))
       (take n)
       reverse
       str/join))


(def pad-zero (partial pad-str \0))


(defn try-call [maybe-fn & args]
  (if (fn? maybe-fn)
    (apply maybe-fn args)
    maybe-fn))


(defn get-next-element [s x]
  (second (drop-while (partial not= x) s)))


(defn get-prev-element [s x]
  (last (take-while (partial not= x) s)))


(defn n-times [n f x]
  (loop [i n, r x]
    (if (pos? i)
      (recur (dec i) (f r))
      r)))


(defn apply-binary-pred [pred x y & more]
  (cond
    (not (pred x y)) false
    (next more)     (recur pred y (first more) (next more))
    :else           (pred y (first more))))


(defn map-v [f m]
  (reduce-kv (fn [acc k _] (update acc k f)) m m))


(defn map-kv [f m]
  (reduce-kv (fn [acc k _] (update acc k (partial f k))) m m))


(def infinite? (partial contains? #{##-Inf ##Inf}))


(def finite? (complement infinite?))


(defn floor [x] (int (Math/floor x)))


(def monotonic? (some-fn (partial apply <=) (partial apply >=)))


(def ffilter (comp first filter))


(def regex? (comp (partial = (type #"")) type))


(defn partition-after
  "Like split-with, but splits after fn returns new result"
  [fn coll]
  (->> (partition-by fn coll)
       (partition 2 2 [])
       (map (partial apply concat))))
