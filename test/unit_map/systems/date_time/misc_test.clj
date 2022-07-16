(ns unit-map.systems.date-time.misc-test
  (:require [unit-map.systems.date-time.misc :as sut]
            [unit-map.core :as umap]
            [clojure.test :as t]))

(def registry-atom (atom nil))

(umap/regseq! registry-atom :sec  #unit-map/seq[0 1 .. 59] :next :min)
(umap/regseq! registry-atom :min  #unit-map/seq[0 1 .. 59] :next :hour)
(umap/regseq! registry-atom :hour #unit-map/seq[0 1 .. 23] :next :day)

(umap/regseq! registry-atom :day   #unit-map/seq[1 2 .. sut/days-in-month] :next :month)
(umap/regseq! registry-atom :month #unit-map/seq[1 2 .. 12] :next :year)
(umap/regseq! registry-atom :year  #unit-map/seq[##-Inf .. -2 -1 1 2 .. ##Inf])

(umap/regsys! registry-atom 'ms-year [:sec :min :hour :day :month :year])

(t/deftest from-epoch-test
  (t/is (= {:day 22, :month 4, :year 2020, :sec 40, :min 49, :hour 1}
           (sut/from-epoch @registry-atom 1587520180)))

  (t/is (= {:day 1, :month 1, :year 1970}
           (sut/from-epoch @registry-atom 0))))

(t/deftest to-epoch-test
  (t/is (= 1587520180
           (sut/to-epoch {:day 22, :month 4, :year 2020, :sec 40, :min 49, :hour 1})))

  (t/is (= 0
           (sut/to-epoch {:day 1, :month 1, :year 1970, :sec 0, :min 0, :hour 0}))))
