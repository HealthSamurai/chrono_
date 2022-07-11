(ns unit-map.core-test
  (:require [unit-map.core :as sut]
            [clojure.test :as t]))


(def registry-atom (atom nil))


(t/deftest defseq-defsys
  (def tregistry-atom (atom nil))

  (sut/defseq tregistry-atom ::a #unit-map/seq[0 1 -> ::b])
  (sut/defseq tregistry-atom ::b #unit-map/seq[0 1 -> ::c])
  (sut/defseq tregistry-atom ::c #unit-map/seq[0 1 -> ::d])
  (sut/defseq tregistry-atom ::d #unit-map/seq[0 1])

  (sut/defseq tregistry-atom ::aa #unit-map/seq[0 2 -> ::b])
  (sut/defseq tregistry-atom ::a #unit-map/seq[0 1 2 3 -> ::c])

  (sut/defseq tregistry-atom ::b2 #unit-map/seq[::b <=> -2 -1 0 -> ::c2])
  (sut/defseq tregistry-atom ::c2 #unit-map/seq[-2 -1 0 -> ::d])
  (sut/defseq tregistry-atom ::c2 #unit-map/seq[-2 -1 .. ##-Inf])

  (sut/defseq tregistry-atom ::b3 #unit-map/seq[::b2 <=> 2 1 0 -> ::c3])
  (sut/defseq tregistry-atom ::c3 #unit-map/seq[2 1 .. ##-Inf])

  (sut/defseq tregistry-atom ::b4 #unit-map/seq[::b <=> 2 1 0 -> ::c4])
  (sut/defseq tregistry-atom ::c4 #unit-map/seq[2 1 .. ##-Inf])

  (sut/defseq tregistry-atom ::b5 #unit-map/seq[2 1 0 -> ::c5])
  (sut/defseq tregistry-atom ::c5 #unit-map/seq[2 1 .. ##-Inf])

  (sut/defseq tregistry-atom ::b6 #unit-map/seq[::b <=> 2 1 0 -> ::c6])
  (sut/defseq tregistry-atom ::c6 #unit-map/seq[::c <=> 2 1 0 -> ::d])
  (sut/defseq tregistry-atom ::c6 #unit-map/seq[2 1 .. ##-Inf])

  (def tregistry @tregistry-atom)

  (t/testing "seqs graph"
    (def graph-assert
      {::a {::b  {:sequence [0 1], :unit ::a, :next-unit ::b}
            ::b2 {:sequence [0 1], :unit ::a, :next-unit ::b2}
            ::b3 {:sequence [0 1], :unit ::a, :next-unit ::b3}
            ::b4 {:sequence [0 1], :unit ::a, :next-unit ::b4}
            ::b6 {:sequence [0 1], :unit ::a, :next-unit ::b6}
            ::c  {:sequence [0 1 2 3], :unit ::a, :next-unit ::c}
            ::c6 {:sequence [0 1 2 3], :unit ::a, :next-unit ::c6}}
       ::b {::c  {:sequence [0 1], :unit ::b, :next-unit ::c}
            ::c6 {:sequence [0 1], :unit ::b, :next-unit ::c6}}
       ::c {::d  {:sequence [0 1], :unit ::c, :next-unit ::d}}
       ::d {nil {:sequence [0 1], :unit ::d}}

       ::aa {::b  {:sequence [0 2], :unit ::aa, :next-unit ::b}
             ::b2 {:sequence [0 2], :unit ::aa, :next-unit ::b2}
             ::b3 {:sequence [0 2], :unit ::aa, :next-unit ::b3}
             ::b4 {:sequence [0 2], :unit ::aa, :next-unit ::b4}
             ::b6 {:sequence [0 2], :unit ::aa, :next-unit ::b6}}

       ::b2 {::c2 {:sequence [-2 -1 0], :unit ::b2, :next-unit ::c2}}
       ::c2 {::d {:sequence [-2 -1 0], :unit ::c2, :next-unit ::d}
             nil {:sequence [{:start -2, :step 1, :end ##-Inf}], :unit ::c2}}

       ::b3 {::c3 {:sequence [2 1 0], :unit ::b3, :next-unit ::c3}}
       ::c3 {nil {:sequence [{:start 2, :step -1, :end ##-Inf}], :unit ::c3}}

       ::b4 {::c4 {:sequence [2 1 0], :unit ::b4, :next-unit ::c4}}
       ::c4 {nil {:sequence [{:start 2, :step -1, :end ##-Inf}], :unit ::c4}}

       ::b5 {::c5 {:sequence [2 1 0], :unit ::b5, :next-unit ::c5}}
       ::c5 {nil {:sequence [{:start 2, :step -1, :end ##-Inf}], :unit ::c5}}

       ::b6 {::c6 {:sequence [2 1 0], :unit ::b6, :next-unit ::c6}
             ::c  {:sequence [2 1 0], :unit ::b6, :next-unit ::c}}
       ::c6 {::d {:sequence [2 1 0], :unit ::c6, :next-unit ::d}
             nil {:sequence [{:start 2, :step -1, :end ##-Inf}], :unit ::c6}}})

    (t/is (= graph-assert (:seqs tregistry)))

    (t/is (= #{#{::a} #{::aa}
               #{::b ::b2 ::b3 ::b4 ::b6} #{::b5}
               #{::c ::c6} #{::c2} #{::c3} #{::c4} #{::c5}
               #{::d}}
             (:eq-units tregistry))))

  (t/testing "valid systems"
    (t/is (sut/sys-continuous? tregistry [::a ::b ::c ::d]))
    (t/is (sut/sys-continuous? tregistry [::a ::b2 ::c2 ::d]))
    (t/is (sut/sys-continuous? tregistry [::a ::b2 ::c2]))
    (t/is (sut/sys-continuous? tregistry [::a ::b3 ::c3]))
    (t/is (sut/sys-continuous? tregistry [::a ::b4 ::c4]))
    (t/is (sut/sys-continuous? tregistry [::b5 ::c5]))
    (t/is (sut/sys-continuous? tregistry [::a ::b6 ::c6 ::d]))
    (t/is (sut/sys-continuous? tregistry [::a ::b6 ::c6]))
    (t/is (sut/sys-continuous? tregistry [::a ::b ::c6 ::d])))

  (t/testing "invalid systems"
    (t/is (not (sut/sys-continuous? tregistry [::d ::c ::b ::a])))
    (t/is (not (sut/sys-continuous? tregistry [::a ::b2 ::c])))
    (t/is (not (sut/sys-continuous? tregistry [::a ::b3 ::c3 ::d])))

    (t/is (not (sut/sys-continuous? tregistry [::a])))))


(do ;;NOTE: seqs
  #_(def si-prefixes
      {:Y  24
       :Z  21
       :E  18
       :P  15
       :T  12
       :G  9
       :M  6
       :k  3
       :h  2
       :da 1
       :_  0
       :d  -1
       :c  -2
       ::m  -3
       :μ  -6
       :n  -9
       :p  -12
       :f  -15
       :a  -18
       :z  -21
       :y  -24})


  (defn leap-year? [{::keys [year]}]
    (and (zero? (rem year 4))
         (or (pos? (rem year 100))
             (zero? (rem year 400)))))


  (defn days-in-month [{:as date, ::keys [month]}]
    (condp contains? month
      #{:jan :mar :may :jul :aug :oct :dec} 31
      #{:apr :jun :sep :nov}                30
      #{:feb}                               (if (leap-year? date) 29 28)
      ##Inf))


  (defn weekday [{::keys [weekday]}]
    (condp contains? weekday
      #{:mon :tue :wed :thu :fri} :workday
      #{:sat :sun}                :weekend))


  (defn season [{::keys [month]}]
    (condp contains? month
      #{:dec :jan :feb} :winter
      #{:mar :apr :may} :spring
      #{:jun :jul :aug} :summer
      #{:sep :oct :nov} :autumn))

  (sut/defseq registry-atom ::ns   #unit-map/seq[0 1 .. 999999999 -> ::sec])

  (sut/defseq registry-atom ::ns   #unit-map/seq[0 1 .. 999999 -> ::ms])
  (sut/defseq registry-atom ::ms   #unit-map/seq[0 1 .. 999 -> ::sec])
  (sut/defseq registry-atom ::sec  #unit-map/seq[0 1 .. 59 -> ::min])
  (sut/defseq registry-atom ::min  #unit-map/seq[0 1 .. 59 -> ::hour])
  (sut/defseq registry-atom ::hour #unit-map/seq[0 1 .. 23 -> ::day])

  (sut/defseq registry-atom ::ms   #unit-map/seq[0 1 .. ##Inf])
  (sut/defseq registry-atom ::ns   #unit-map/seq[0 1 .. ##Inf])
  (sut/defseq registry-atom ::sec  #unit-map/seq[0 1 .. ##Inf])
  (sut/defseq registry-atom ::hour #unit-map/seq[0 1 .. ##Inf])
  (sut/defseq registry-atom ::day  #unit-map/seq[0 1 .. ##Inf]) #_"NOTE: should start with 0 or with 1?"

  (sut/defseq registry-atom ::ampm-hour   #unit-map/seq[::hour <=> 12 1 2 .. 11 -> ::ampm-period])
  (sut/defseq registry-atom ::ampm-period #unit-map/seq[:am :pm -> ::day])

  (sut/defseq registry-atom ::day   #unit-map/seq[1 2 .. days-in-month -> ::month])
  (sut/defseq registry-atom ::month #unit-map/seq[:jan :feb  :mar :apr :may  :jun :jul :aug  :sep :oct :nov  :dec -> ::year])
  (sut/defseq registry-atom ::year  #unit-map/seq[##-Inf .. -2 -1 1 2 .. ##Inf])

  (sut/defseq registry-atom ::weekday  #unit-map/seq[::day <=> :mon :tue :wed :thu :fri :sat :sun -> ::week])
  (sut/defseq registry-atom ::week     #unit-map/seq[1 2 .. 52])
  (sut/defseq registry-atom ::weekpart #unit-map/seq[::weekday <=> weekday])
  (sut/defseq registry-atom ::season   #unit-map/seq[::month <=> season])

  (sut/defseq registry-atom ::mil  #unit-map/seq[0 1 .. 999  -> ::inch])
  (sut/defseq registry-atom ::inch #unit-map/seq[0 1 .. 11   -> ::foot])
  (sut/defseq registry-atom ::foot #unit-map/seq[0 1 .. 5279 -> ::mile])
  (sut/defseq registry-atom ::mile #unit-map/seq[0 1 .. ##Inf])

  (sut/defseq registry-atom ::mm #unit-map/seq[0 1 .. 9   -> ::cm])
  (sut/defseq registry-atom ::cm #unit-map/seq[0 1 .. 99  -> ::m])
  (sut/defseq registry-atom ::m  #unit-map/seq[0 1 .. 999 -> ::km])
  (sut/defseq registry-atom ::km #unit-map/seq[0 1 .. ##Inf])

  (sut/defseq registry-atom ::epoch-year  #unit-map/seq[::year <=>
                                                        (fn [{::keys [epoch]}]
                                                          (if (= :BC epoch) ##Inf 1))
                                                        (fn [{::keys [epoch]}]
                                                          (if (= :BC epoch) -1 1))
                                                        ..
                                                        (fn [{::keys [epoch]}]
                                                          (if (= :BC epoch) 1 ##Inf))
                                                        -> ::epoch])

  (sut/defseq registry-atom ::epoch  #unit-map/seq[:BC :AD]))

(do ;;NOTE: systems
  (sut/defsys registry-atom 'imperial [::mil ::inch ::foot ::mile])

  (sut/defsys registry-atom 'metric   [::mm ::cm ::m ::km])

  (sut/defsys registry-atom 'ms-hour    [::ms ::sec ::min ::hour])
  (sut/defsys registry-atom 'ns-hour    [::ns ::sec ::min ::hour])
  (sut/defsys registry-atom 'ns-ms-hour [::ns ::ms ::sec ::min ::hour])

  (sut/defsys registry-atom 'timestamp    [::ms])
  (sut/defsys registry-atom 'ns-timestamp [::ns])

  (sut/defsys registry-atom 'seconds [::ns ::ms ::sec])
  (sut/defsys registry-atom 'ns-seconds [::ns ::sec])

  (sut/defsys registry-atom 'ms-day    [::ms ::sec ::min ::hour ::day])
  (sut/defsys registry-atom 'ns-day    [::ns ::sec ::min ::hour ::day])
  (sut/defsys registry-atom 'ns-ms-day [::ns ::ms ::sec ::min ::hour ::day])

  (sut/defsys registry-atom 'ms-day-am-pm    [::ms ::sec ::min ::ampm-hour ::ampm-period ::day])
  (sut/defsys registry-atom 'ns-day-am-pm    [::ns ::sec ::min ::ampm-hour ::ampm-period ::day])
  (sut/defsys registry-atom 'ns-ms-day-am-pm [::ns ::ms ::sec ::min ::ampm-hour ::ampm-period ::day])

  (sut/defsys registry-atom 'date       [::day ::month ::year])
  (sut/defsys registry-atom 'month-year [::month ::year])

  (sut/defsys registry-atom 'ms-year    [::ms ::sec ::min ::hour ::day ::month ::year])
  (sut/defsys registry-atom 'ns-year    [::ns ::sec ::min ::hour ::day ::month ::year])
  (sut/defsys registry-atom 'ns-ms-year [::ns ::ms ::sec ::min ::hour ::day ::month ::year])

  (sut/defsys registry-atom 'ms-year-am-pm    [::ms ::sec ::min ::ampm-hour ::ampm-period ::day ::month ::year])
  (sut/defsys registry-atom 'ns-year-am-pm    [::ns ::sec ::min ::ampm-hour ::ampm-period ::day ::month ::year])
  (sut/defsys registry-atom 'ns-ms-year-am-pm [::ns ::ms ::sec ::min ::ampm-hour ::ampm-period ::day ::month ::year])

  (sut/defsys registry-atom 'weeks [::weekday ::week])

  (sut/defsys registry-atom 'ms-year-epoch [::ms ::sec ::min ::hour ::day ::month ::epoch-year ::epoch])
  (sut/defsys registry-atom 'year-epoch [::epoch-year ::epoch])

  (->> (for [[sys sys-def] (:systems @registry-atom)
             :when (symbol? sys)]
         (list 'def sys sys-def))
       (cons 'do)
       eval #_"TODO: refactor this"))


(t/deftest sys-detection
  (t/is (= ms-hour
           (sut/guess-sys @registry-atom {::min 30, ::hour 15})))

  (t/is (= ms-day-am-pm
           (sut/guess-sys @registry-atom {::min 30, ::ampm-hour 3, ::ampm-period :pm})))

  (t/is (= ns-ms-day
           (sut/guess-sys @registry-atom {::ns 1, ::ms 1, ::sec 1, ::min 1, ::hour 1, ::day 1})))

  (t/is (= ns-ms-hour
           (sut/guess-sys @registry-atom {::ns 1, ::ms 1, ::sec 1, ::min 1, ::hour 25})))

  (t/is (= ns-ms-hour
           (sut/guess-sys @registry-atom {::ns 1, ::ms 1, ::sec 1, ::min 1501})))

  (t/is (= seconds
           (sut/guess-sys @registry-atom {::ns 1, ::ms 1, ::sec 90061})))

  (t/is (= seconds
           (sut/guess-sys @registry-atom {::ns 1, ::ms 90061001})))

  (t/is (= ns-timestamp
           (sut/guess-sys @registry-atom {::ns 90061001000001})))


  (t/is (= ns-day
           (sut/guess-sys @registry-atom {::ns 1000001, ::sec 1, ::min 1, ::hour 1, ::day 1})))

  (t/is (= ns-hour
           (sut/guess-sys @registry-atom {::ns 1000001, ::sec 1, ::min 1, ::hour 25})))

  (t/is (= ns-hour
           (sut/guess-sys @registry-atom {::ns 1000001, ::sec 1, ::min 1501})))

  (t/is (= ns-seconds
           (sut/guess-sys @registry-atom {::ns 1000001, ::sec 90061})))

  (t/is (= ns-timestamp
           (sut/guess-sys @registry-atom {::ns 90061001000001})))

  (t/is (= ns-day
           (sut/guess-sys @registry-atom {::ns 1, ::sec 1, ::min 1, ::hour 1, ::day 1 :delta {::ns 1}})))


  (t/is (= ms-day
           (sut/guess-sys @registry-atom {::ms 1, ::sec 1, ::min 1, ::hour 1, ::day 1})))

  (t/is (= ms-hour
           (sut/guess-sys @registry-atom {::ms 1, ::sec 1, ::min 1, ::hour 25})))

  (t/is (= ms-hour
           (sut/guess-sys @registry-atom {::ms 1, ::sec 1, ::min 1501})))

  (t/is (= ms-day
           (sut/guess-sys @registry-atom {::ms 1, ::sec 1, ::min 1501}
                          ::day)))

  (t/is (= seconds
           (sut/guess-sys @registry-atom {::ms 1, ::sec 90061})))

  (t/is (= timestamp
           (sut/guess-sys @registry-atom {::ms 90061001})))

  (t/is (= ms-day
           (sut/guess-sys @registry-atom {::ms 1, ::sec 1, ::min 1, ::hour 1, ::day 1 :delta {::ms 1}})))

  (t/is (nil? (sut/guess-sys @registry-atom {})))

  (t/is (nil? (sut/guess-sys @registry-atom nil))))


(t/deftest find-diff-branches-unit-test
  (sut/find-diff-branches [::ns ::ms ::sec ::min ::ampm-hour ::ampm-period ::day ::month ::year]
                          [::ns ::sec ::min ::hour :period ::day ::month ::year])
  ;; => [::ns
  ;;     [[::ms] []]
  ;;     ::sec
  ;;     ::min
  ;;     [[::ampm-hour ::ampm-period] [::hour :period]]
  ;;     ::day
  ;;     ::month
  ;;     ::year]

  (t/is (= [1 11 2 22 3 6 4 44 5 55]
           (sut/find-diff-branches [1 11 2 22 3 6 4 44 5 55]
                                   [1 11 2 22 3 6 4 44 5 55])))

  (t/is (= [1 11 [[2 22] [88]] 3 6 [[4 44] [99 9]] 5 55]
           (sut/find-diff-branches [1 11 2 22 3 6 4 44 5 55]
                                   [1 11 88 3 6 99 9 5 55])))

  (t/is (= [1 11 [[2] []] 3]
           (sut/find-diff-branches [1 11 2 3]
                                   [1 11 3])))

  (t/is (= [1 11 [[] [3]]]
           (sut/find-diff-branches [1 11]
                                   [1 11 3])))

  (t/is (= [1 11 [[4] [3]]]
           (sut/find-diff-branches [1 11 4]
                                   [1 11 3])))

  (t/is (= [[[] [1 2]] 11 3]
           (sut/find-diff-branches [11 3]
                                   [1 2 11 3])))

  (t/is (= [[[] [1]] 11 3]
           (sut/find-diff-branches [11 3]
                                   [1 11 3])))

  (t/is (= [[[] [1]] 11 3 [[] [4]]]
           (sut/find-diff-branches [11 3]
                                   [1 11 3 4])))

  (t/is (= [[[2] [1]] 11 3]
           (sut/find-diff-branches [2 11 3]
                                   [1 11 3])))

  (t/is (= [1 2 [[3] []] 4 5]
           (sut/find-diff-branches [1 2 3 4 5]
                                   [1 2 4 5])))

  (t/is (= [[[] [1 11 3]]]
           (sut/find-diff-branches []
                                   [1 11 3])))


  (t/is (= [1 11 [[88] [2 22]] 3 6 [[99 9] [4 44]] 5 55]
           (sut/find-diff-branches [1 11 88 3 6 99 9 5 55]
                                   [1 11 2 22 3 6 4 44 5 55])))

  (t/is (= [1 11 [[] [2]] 3]
           (sut/find-diff-branches [1 11 3]
                                   [1 11 2 3])))

  (t/is (= [1 11 [[3] []]]
           (sut/find-diff-branches [1 11 3]
                                   [1 11])))

  (t/is (= [[[1 2] []] 11 3]
           (sut/find-diff-branches [1 2 11 3]
                                   [11 3])))

  (t/is (= [[[1] []] 11 3]
           (sut/find-diff-branches [1 11 3]
                                   [11 3])))

  (t/is (= [[[1] []] 11 3 [[4] []]]
           (sut/find-diff-branches [1 11 3 4]
                                   [11 3])))


  (t/is (= [[[1 2] []] 3 4]
           (sut/find-diff-branches [1 2 3 4]
                                   [3 4])))

  (t/is (= [1 [[2] []] 3 4]
           (sut/find-diff-branches [1 2 3 4]
                                   [1 3 4])))

  (t/is (= [[[1 11 3] []]]
           (sut/find-diff-branches [1 11 3]
                                   [])))

  (t/is (= nil
           (sut/find-diff-branches []
                                   [])))

  (t/is (= [[[1 2 3] [::a ::b ::c]]]
           (sut/find-diff-branches [1 2 3] [::a ::b ::c]))))


(t/deftest sys-conversion
  (t/testing "interseciton"
    (t/is (= ms-year
             (sut/sys-intersection @registry-atom
                                   {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, :tz {::hour 2}}
                                   {:delta {::hour 3}})))

    (t/is (= ms-year
             (sut/sys-intersection @registry-atom
                                   {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, :tz {::hour 2}}
                                   {::year 2021, ::month :sep, ::day 7, ::hour 22, ::min 30, :tz {::hour 3}})))

    (t/is (= ms-year
             (sut/sys-intersection @registry-atom
                                   {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, :tz {::hour 2}}
                                   {::year 2021})))

    (t/is (nil? (sut/sys-intersection @registry-atom
                                      {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, :tz {::hour 2}}
                                      {::cm 49})))

    (t/is (= ms-year
             (sut/sys-intersection @registry-atom
                                   {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, :tz {::hour 2}}
                                   {})))

    (t/is (= ms-year
             (sut/sys-intersection @registry-atom
                                   {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, :tz {::hour 2}}))))

  (t/testing "find conversion"
    #_"TODO: timezones"

    (t/is (= [{[::ms]    [::ms]}
              {[::sec]   [::sec]}
              {[::min]   [::min]}
              {[::hour]  [::hour]}
              {[::day]   [::day]}
              {[::month] [::month]}
              {[::year]  [::year]}]
             (sut/find-conversion
               @registry-atom
               {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30}
               {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30})))

    (t/is (= [{[::ms]    [::ms]}
              {[::sec]   [::sec]}
              {[::min]   [::min]}
              {[::hour]  [::ampm-hour ::ampm-period]}
              {[::day]   [::day]}
              {[::month] [::month]}
              {[::year]  [::year]}]
             (sut/find-conversion
               @registry-atom
               {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30}
               {::year 2021, ::month :sep, ::day 7, ::ampm-period :pm, ::ampm-hour 9, ::min 30})))

    (t/testing "different start"
      (t/is (= [{[]       [::ns]}
                {[::ms]    [::ms]}
                {[::sec]   [::sec]}
                {[::min]   [::min]}
                {[::hour]  [::ampm-hour ::ampm-period]}
                {[::day]   [::day]}
                {[::month] [::month]}
                {[::year]  [::year]}]
               (sut/find-conversion
                 @registry-atom
                 {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, ::sec 10, ::ms 10}
                 {::year 2021, ::month :sep, ::day 7, ::ampm-period :pm, ::ampm-hour 9, ::min 30, ::sec 10, ::ms 10, ::ns 10}))))

    (t/testing "two parallel graph paths"
      (t/is (= [{[::ns]    [::ns]}
                {[]       [::ms]}
                {[::sec]   [::sec]}
                {[::min]   [::min]}
                {[::hour]  [::ampm-hour ::ampm-period]}
                {[::day]   [::day]}
                {[::month] [::month]}
                {[::year]  [::year]}]
               (sut/find-conversion
                 @registry-atom
                 {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, ::sec 10, ::ns 10000010}
                 {::year 2021, ::month :sep, ::day 7, ::ampm-period :pm, ::ampm-hour 9, ::min 30, ::sec 10, ::ms 10, ::ns 10}))))

    (t/testing "no conversion"
      (t/is (empty?
              (sut/find-conversion
                @registry-atom
                {::year 2021, ::month :sep, ::day 7, ::hour 21, ::min 30, ::sec 10, ::ns 10000010}
                {::m 1, ::cm 82}))))

    (t/testing "no common units, no common finish"
      (t/is (= [{[::weekday ::week] [::day ::month ::year]}]
               (sut/find-conversion
                 @registry-atom
                 {::week 6}
                 {::year 2022, ::month :jan, ::day 1}))))))


(t/deftest seq-range-utils-test
  (t/testing "static? dynamic?"
    (t/is (true? (sut/static-sequence? #unit-map/seq[0 1 .. 9])))

    (t/is (false? (sut/static-sequence? #unit-map/seq[0 1 .. (fn [_] 9)])))

    (t/is (true? (sut/dynamic-sequence? #unit-map/seq[0 1 .. (fn [_] 9)])))

    (t/is (false? (sut/dynamic-sequence? #unit-map/seq[0 1 .. 9]))))

  (t/testing "concretize range"
    (t/is (= {:start 0, :step 1, :end 9}
             (sut/concretize-range (-> #unit-map/seq[(fn [_] 0) (fn [_] 1) .. (fn [_] 9)]
                                       :sequence
                                       first)
                                   nil)))

    (t/is (= {:start 0, :step 1, :end 9}
             (sut/concretize-range (-> #unit-map/seq[0 1 .. 9]
                                       :sequence
                                       first)
                                   nil)))

    (t/is (= {:start 0, :step 1, :end 9}
             (sut/concretize-range (-> #unit-map/seq[(fn [_] 0) .. (fn [_] 1) (fn [_] 9)]
                                       :sequence
                                       first)
                                   nil)))

    (t/is (= {:start 0, :step 1, :end 9}
             (sut/concretize-range (-> #unit-map/seq[0 .. 8 9]
                                       :sequence
                                       first)
                                   nil)))

    (t/is (= {:start 1, :step 1, :end 28}
             (sut/concretize-range (-> #unit-map/seq[1 2 .. (fn [{::keys [month]}] (if (= :feb month) 28 30))]
                                       :sequence
                                       first)
                                   {::day 1, ::month :feb, ::year 2022}))))

  (t/testing "seq length"
    (t/is (= 10 (sut/sequence-length #unit-map/seq[0 1 .. 9]
                                     nil)))

    (t/is (= ##Inf (sut/sequence-length #unit-map/seq[0 1 .. ##Inf]
                                        nil)))

    (t/is (= 10 (sut/sequence-length #unit-map/seq[-9 -8 .. 0]
                                     nil)))

    (t/is (= 10 (sut/sequence-length #unit-map/seq[-9 .. -1 0]
                                     nil)))

    (t/is (= ##Inf (sut/sequence-length #unit-map/seq[##-Inf .. -1 0]
                                        nil)))

    (t/is (= ##Inf (sut/sequence-length #unit-map/seq[##-Inf .. -1 0 1 2 .. ##Inf]
                                        nil))))

  (t/testing "first index"
    (t/is (= 0 (sut/sequence-first-index #unit-map/seq[0 1 .. 9]
                                         nil)))

    (t/is (= 0 (sut/sequence-first-index #unit-map/seq[0 1 .. ##Inf]
                                         nil)))

    (t/is (= ##-Inf (sut/sequence-first-index #unit-map/seq[##-Inf .. -1 0]
                                              nil)))

    (t/is (= ##-Inf (sut/sequence-first-index #unit-map/seq[##-Inf .. -1 0 1 2 .. ##Inf]
                                              nil))))

  (t/testing "last index"
    (t/is (= 9 (sut/sequence-last-index #unit-map/seq[0 1 .. 9]
                                        nil)))

    (t/is (= 11 (sut/sequence-last-index #unit-map/seq[0 1 .. 9 10 11]
                                         nil)))

    (t/is (= 11 (sut/sequence-last-index #unit-map/seq[-2 -1 0 1 .. 9]
                                         nil)))

    (t/is (= ##Inf (sut/sequence-last-index #unit-map/seq[-1 0 1 .. ##Inf]
                                            nil)))

    (t/is (= ##Inf #_"TODO: probably should be 1"
             (sut/sequence-last-index
               #unit-map/seq[##-Inf .. -1 0 1]
               nil)))

    (t/is (= ##Inf (sut/sequence-last-index #unit-map/seq[##-Inf .. -1 0 1 2 .. ##Inf]
                                            nil))))

  (t/testing "contains"
    (t/is (some? (sut/sequence-contains-some
                   #unit-map/seq[##-Inf .. -2 -1 1 2 3 .. ##Inf]
                   nil
                   10)))

    (t/is (some? (sut/sequence-contains-some
                   #unit-map/seq[##-Inf .. -2 -1 1 2 3 .. ##Inf]
                   nil
                   -10)))

    (t/is (some? (sut/sequence-contains-some
                   #unit-map/seq[##-Inf .. -2 -1 1 2 3 .. ##Inf]
                   nil
                   1)))

    (t/is (some? (sut/sequence-contains-some
                   #unit-map/seq[##-Inf .. -2 -1 1 2 3 .. ##Inf]
                   nil
                   ##Inf)))

    (t/is (some? (sut/sequence-contains-some
                   #unit-map/seq[##-Inf .. -2 -1 1 2 3 .. ##Inf]
                   nil
                   ##-Inf)))

    (t/is (nil? (sut/sequence-contains-some
                  #unit-map/seq[##-Inf .. -2 -1 1 2 3 .. ##Inf]
                  nil
                  0))))

  (t/testing "index-of"
    (t/is (= 11 (sut/sequence-index-of #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                       nil
                                       10)))

    (t/is (= -8 (sut/sequence-index-of #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                       nil
                                       -10)))

    (t/is (= 2 (sut/sequence-index-of #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                      nil
                                      1)))

    (t/is (= ##Inf (sut/sequence-index-of #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                          nil
                                          ##Inf)))

    (t/is (= ##-Inf (sut/sequence-index-of #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                           nil
                                           ##-Inf))))

  (t/testing "nth"
    (t/testing "index-of"
      (t/is (= 10 (sut/sequence-nth #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                    nil
                                    11)))

      (t/is (= -10 (sut/sequence-nth #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                     nil
                                     -8)))

      (t/is (= 1 (sut/sequence-nth #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                   nil
                                   2)))

      (t/is (= ##Inf (sut/sequence-nth #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                       nil
                                       ##Inf)))

      (t/is (= ##-Inf (sut/sequence-nth #unit-map/seq[##-Inf .. -3 -2 -1 1 2 3 .. ##Inf]
                                        nil
                                        ##-Inf))))))


(t/deftest sys-utils-test
  (t/testing "next/prev unit"
    (t/is (= ::month
             (sut/get-next-unit @registry-atom
                                {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                ::day)))

    (t/is (= ::hour
             (sut/get-prev-unit @registry-atom
                                {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                ::day)))

    (t/is (= nil
             (sut/get-next-unit @registry-atom
                                {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                ::year)))

    (t/is (= ::sec
             (sut/get-prev-unit @registry-atom
                                {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                ::min)))

    (t/is (= ::ms
             (sut/get-prev-unit @registry-atom
                                {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                ::sec)))

    (t/is (= nil
             (sut/get-prev-unit @registry-atom
                                {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                ::ms)))

    (t/is (= ::year
             (sut/get-next-unit @registry-atom
                                {::min 30}
                                ::month)))

    (t/is (= ::day
             (sut/get-prev-unit @registry-atom
                                {::min 30}
                                ::month))))

  (t/testing "get-unit-seq"
    (t/is (= [:jan :feb  :mar :apr :may  :jun :jul :aug  :sep :oct :nov  :dec]
             (:sequence (sut/get-unit-seq @registry-atom
                                          {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                          ::month))))

    (t/is (= [:jan :feb  :mar :apr :may  :jun :jul :aug  :sep :oct :nov  :dec]
             (:sequence (sut/get-unit-seq @registry-atom
                                          {::min 30}
                                          ::month))))

    (t/is (= (:sequence #unit-map/seq[##-Inf .. -2 -1 1 2 .. ##Inf])
             (:sequence (sut/get-unit-seq @registry-atom
                                          {::year 2022 ::month :jun ::day 4 ::hour 12 ::min 30}
                                          ::year)))))

  (t/testing "get-next-unit-value"
    (t/is (= (range 60)
             (->> (iterate #(sut/get-next-unit-value
                              (get-in @registry-atom [:seqs ::sec ::min])
                              nil
                              %)
                           0)
                  (take-while some?))))

    (t/is (= [:jan :feb  :mar :apr :may  :jun :jul :aug  :sep :oct :nov  :dec]
             (->> (iterate #(sut/get-next-unit-value
                              (get-in @registry-atom [:seqs ::month ::year])
                              nil
                              %)
                           :jan)
                  (take-while some?))))

    (t/is (= (range 1970 2021)
             (->> (iterate #(sut/get-next-unit-value
                              (get-in @registry-atom [:seqs ::year nil])
                              nil
                              %)
                           1970)
                  (take 51))))

    (t/is (= [12 1 2 3 4 5 6 7 8 9 10 11]
             (->> (iterate #(sut/get-next-unit-value
                              (get-in @registry-atom [:seqs ::ampm-hour ::ampm-period])
                              nil
                              %)
                           12)
                  (take-while some?))))

    (t/is (= 13 (sut/get-next-unit-value #unit-map/seq[1 3 .. :TODO/remove (fn [{::keys [bar]}] (if (odd? bar) 9 11)) 13 15]
                                         {::bar 7}
                                         9)))

    (t/is (= 11 (sut/get-next-unit-value #unit-map/seq[1 3 .. :TODO/remove (fn [{::keys [bar]}] (if (odd? bar) 9 11)) 13 15]
                                         {::bar 8}
                                         9))))

  (t/testing "get-prev-unit-value"
    (t/is (= (reverse (range 60))
             (->> (iterate #(sut/get-prev-unit-value
                              (get-in @registry-atom [:seqs ::sec ::min])
                              nil
                              %)
                           59)
                  (take-while some?))))

    (t/is (= (reverse [:jan :feb  :mar :apr :may  :jun :jul :aug  :sep :oct :nov  :dec])
             (->> (iterate #(sut/get-prev-unit-value
                              (get-in @registry-atom [:seqs ::month ::year])
                              nil
                              %)
                           :dec)
                  (take-while some?))))

    (t/is (= (reverse (range 1970 2021))
             (->> (iterate #(sut/get-prev-unit-value
                              (get-in @registry-atom [:seqs ::year nil])
                              nil
                              %)
                           2020)
                  (take 51))))

    (t/is (= (reverse [12 1 2 3 4 5 6 7 8 9 10 11])
             (->> (iterate #(sut/get-prev-unit-value
                              (get-in @registry-atom [:seqs ::ampm-hour ::ampm-period])
                              nil
                              %)
                           11)
                  (take-while some?))))

    (t/is (= 9 (sut/get-prev-unit-value #unit-map/seq[1 3 .. :TODO/remove (fn [{::keys [bar]}] (if (odd? bar) 9 11)) 13 15]
                                        {::bar 7}
                                        13)))

    (t/is (= 11 (sut/get-prev-unit-value #unit-map/seq[1 3 .. :TODO/remove (fn [{::keys [bar]}] (if (odd? bar) 9 11)) 13 15]
                                         {::bar 8}
                                         13))))

  (t/testing "get first/last el"
    (t/is (= 0 (sut/get-first-el #unit-map/seq[0 1 3 .. :TODO/remove (fn [{::keys [bar]}] (if (odd? bar) 9 11)) 13 15]
                                 {})))

    (t/is (= 15 (sut/get-last-el #unit-map/seq[0 1 3 .. :TODO/remove (fn [{::keys [bar]}] (if (odd? bar) 9 11)) 13 15]
                                 {})))

    (t/is (= 1 (sut/get-first-el #unit-map/seq[(constantly 1) (constantly 1) .. :TODO/remove (constantly 10)]
                                 {})))

    (t/is (= 10 (sut/get-last-el #unit-map/seq[(constantly 1) (constantly 1) .. :TODO/remove (constantly 10)]
                                 {}))))

  (t/testing "get min/max value"
    (t/is (= ##-Inf (sut/get-min-value @registry-atom {::year 2022} ::year)))

    (t/is (= ##Inf (sut/get-max-value @registry-atom {::year 2022} ::year)))))


(t/deftest inc-dec-test
  (t/testing "inc-unit"
    (t/testing "am-pm clock"
      (def value {::ampm-hour 12, ::ampm-period :am})

      (t/is (= [{::ampm-hour 12, ::ampm-period :am} {::ampm-hour 1, ::ampm-period :am} {::ampm-hour 2, ::ampm-period :am}
                {::ampm-hour 3, ::ampm-period :am} {::ampm-hour 4, ::ampm-period :am} {::ampm-hour 5, ::ampm-period :am}
                {::ampm-hour 6, ::ampm-period :am} {::ampm-hour 7, ::ampm-period :am} {::ampm-hour 8, ::ampm-period :am}
                {::ampm-hour 9, ::ampm-period :am} {::ampm-hour 10, ::ampm-period :am} {::ampm-hour 11, ::ampm-period :am}

                {::ampm-hour 12, ::ampm-period :pm} {::ampm-hour 1, ::ampm-period :pm} {::ampm-hour 2, ::ampm-period :pm}
                {::ampm-hour 3, ::ampm-period :pm} {::ampm-hour 4, ::ampm-period :pm} {::ampm-hour 5, ::ampm-period :pm}
                {::ampm-hour 6, ::ampm-period :pm} {::ampm-hour 7, ::ampm-period :pm} {::ampm-hour 8, ::ampm-period :pm}
                {::ampm-hour 9, ::ampm-period :pm} {::ampm-hour 10, ::ampm-period :pm} {::ampm-hour 11, ::ampm-period :pm}]
               (take 24 (iterate (partial sut/inc-unit @registry-atom ::ampm-hour) value)))))

    (t/testing "epoch"
      (t/is (= [{::year -2} {::year -1} {::year 1} {::year 2}]
               (take 4 (iterate #(sut/inc-unit @registry-atom ::year %) {::year -2}))))

      (t/is (= [{::epoch :BC, ::epoch-year 2} {::epoch :BC, ::epoch-year 1}
                {::epoch :AD, ::epoch-year 1} {::epoch :AD, ::epoch-year 2}]
               (take 4 (iterate #(sut/inc-unit @registry-atom ::epoch-year %) {::epoch :BC ::epoch-year 2})))))

    (t/testing "calendar"
      (def value {::day 1, ::month :jan, ::year 2020})

      (def calendar (->> value
                         (iterate (partial sut/inc-unit @registry-atom ::day))
                         (take-while (comp #{2020} ::year))
                         (partition-by ::month)))

      (t/is (= 12 (count calendar)))
      (t/is (= 366 (count (flatten calendar))))))

  (t/testing "dec-unit"
    (t/testing "am-pm clock"
      (def value {::ampm-hour 11, ::ampm-period :pm})

      (t/is (= [{::ampm-hour 11, ::ampm-period :pm} {::ampm-hour 10, ::ampm-period :pm} {::ampm-hour 9, ::ampm-period :pm}
                {::ampm-hour 8, ::ampm-period :pm} {::ampm-hour 7, ::ampm-period :pm} {::ampm-hour 6, ::ampm-period :pm}
                {::ampm-hour 5, ::ampm-period :pm} {::ampm-hour 4, ::ampm-period :pm} {::ampm-hour 3, ::ampm-period :pm}
                {::ampm-hour 2, ::ampm-period :pm} {::ampm-hour 1, ::ampm-period :pm} {::ampm-hour 12, ::ampm-period :pm}

                {::ampm-hour 11, ::ampm-period :am} {::ampm-hour 10, ::ampm-period :am} {::ampm-hour 9, ::ampm-period :am}
                {::ampm-hour 8, ::ampm-period :am} {::ampm-hour 7, ::ampm-period :am} {::ampm-hour 6, ::ampm-period :am}
                {::ampm-hour 5, ::ampm-period :am} {::ampm-hour 4, ::ampm-period :am} {::ampm-hour 3, ::ampm-period :am}
                {::ampm-hour 2, ::ampm-period :am} {::ampm-hour 1, ::ampm-period :am} {::ampm-hour 12, ::ampm-period :am}]
               (take 24 (iterate (partial sut/dec-unit @registry-atom ::ampm-hour) value)))))

    (t/testing "calendar"
      (def value {::day 31, ::month :dec, ::year 2019})

      (def calendar (->> value
                         (iterate (partial sut/dec-unit @registry-atom ::day))
                         (take-while (comp #{2019} ::year))
                         (partition-by ::month)))

      (t/is (= 12 (count calendar)))
      (t/is (= 365 (count (flatten calendar)))))))


(t/deftest cmp
  (t/testing "eq?"
    (t/is (sut/eq? @registry-atom
                   {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/eq? @registry-atom
                   {::day 26, ::month :jul, ::year 2020} {::day 26, ::month :jul, ::year 2020} {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/eq? @registry-atom
                   {} {}))
    (t/is (sut/not-eq? @registry-atom {} {::year 2020})))

  (t/testing "not-eq?"
    (t/is (not (sut/not-eq? @registry-atom {::day 26, ::month :jul, ::year 2020})))
    (t/is (sut/not-eq? @registry-atom {::day 25, ::month :jul, ::year 2020} {::day 26, ::month :jul, ::year 2020} {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/not-eq? @registry-atom {} {::day 26, ::month :jul, ::year 2020})))

  (t/testing "lt?"
    (t/is (sut/lt? @registry-atom {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/lt? @registry-atom {::day 26, ::month :jul, ::year 2020} {::day 27, ::month :jul, ::year 2020} {::day 28, ::month :jul, ::year 2020}))
    (t/is (sut/lt? @registry-atom {} {::day 26, ::month :jul, ::year 2020})))

  (t/testing "gt?"
    (t/is (sut/gt? @registry-atom {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/gt? @registry-atom {::day 27, ::month :jul, ::year 2020} {::day 26, ::month :jul, ::year 2020} {::day 25, ::month :jul, ::year 2020}))
    (t/is (sut/gt? @registry-atom {::day 26, ::month :jul, ::year 2020} {})))

  (t/testing "lte?"
    (t/is (sut/lte? @registry-atom {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/lte? @registry-atom {::day 26, ::month :jul, ::year 2020} {::day 27, ::month :jul, ::year 2020} {::day 27, ::month :jul, ::year 2020}))
    (t/is (sut/lte? @registry-atom {} {::day 26, ::month :jul, ::year 2020})))

  (t/testing "gte?"
    (t/is (sut/gte? @registry-atom {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/gte? @registry-atom {::day 27, ::month :jul, ::year 2020} {::day 26, ::month :jul, ::year 2020} {::day 26, ::month :jul, ::year 2020}))
    (t/is (sut/gte? @registry-atom {::day 26, ::month :jul, ::year 2020} {}))))


(t/deftest arithmetic
  (t/testing "add-to-unit"
    (t/is (= {::hour 0, ::day 22, ::month :aug, ::year 2044}
             (sut/add-to-unit @registry-atom {::hour 0 ::day 1, ::month :jan, ::year 2020} ::hour 216000)))
    (t/is (= {::hour 0 ,::year 1995, ::month :may, ::day 12}
             (sut/add-to-unit @registry-atom {::hour 0 ::day 1, ::month :jan, ::year 2020} ::hour -216000)))
    (t/is (= {::day 1, ::month :jan, ::year 2020}
             (sut/add-to-unit @registry-atom {::day 1, ::month :jan, ::year 2020} ::hour 0))))

  (t/testing "subtract-from-unit"
    (t/is (= {::hour 0, ::day 22, ::month :aug, ::year 2044}
             (sut/subtract-from-unit @registry-atom {::day 1, ::month :jan, ::year 2020} ::hour -216000)))
    (t/is (= {::hour 0,::year 1995, ::month :may, ::day 12}
             (sut/subtract-from-unit @registry-atom {::day 1, ::month :jan, ::year 2020} ::hour 216000)))
    (t/is (= {::day 1, ::month :jan, ::year 2020}
             (sut/subtract-from-unit @registry-atom {::day 1, ::month :jan, ::year 2020} ::hour 0))))

  (t/testing "+"
    (def t
      {::year  2018
       ::month :jan
       ::day   1
       ::hour  12
       ::min   30
       ::sec   30
       ::ms    500})

    (t/is (= (merge t {::ms 700})
             (sut/add-delta @registry-atom
                            t {::ms 200})))

    (t/is (= (merge t {::ms 100, ::sec 31})
             (sut/add-delta @registry-atom
                            t {::ms 600})))

    (t/is (= {::ms 1500}
             (sut/add-delta @registry-atom
                            {::ms 600} {::ms 600} {::ms 300})))

    (t/is (= {::ms 500, ::sec 1}
             (sut/add-delta @registry-atom
                            {::sec 0, ::ms 600} {::ms 600} {::ms 300})))

    (t/is (= (merge t {::sec 50})
             (sut/add-delta @registry-atom
                            t {::sec 20})))

    (t/is (= (merge t {::sec 50})
             (sut/add-delta @registry-atom
                            t {::sec 20})))

    (t/is (= (merge t {::hour 12, ::min 50})
             (sut/add-delta @registry-atom
                            t {::min 20})))

    (t/is (= (merge t {::hour 13 ::min 0})
             (sut/add-delta @registry-atom
                            t {::min 30})))

    (t/is (= {::year 2019 ::month :jan ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2018 ::month :dec ::day 31} {::day 1})))

    (t/is (= {::year 2018 ::month :feb ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2018 ::month :jan ::day 1} {::day 31})))

    (t/is (= {::year 2020 ::month :jan ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2018 ::month :dec ::day 31} {::day 366})))

    (t/is (= {::year 2018 ::month :mar ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2018 ::month :feb ::day 28} {::day 1})))

    (t/is (= {::year 2018 ::month :mar ::day 31}
             (sut/add-delta @registry-atom
                            {::year 2018 ::month :mar ::day 30} {::day 1})))

    (t/is (= {::year 2018 ::month :apr ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2018 ::month :mar ::day 31} {::day 1})))

    (t/is (= {::ms 400}
             (sut/add-delta @registry-atom
                            {::ms 100} {::ms 300})))

    (t/is (= {::ms 200 ::sec 1}
             (sut/add-delta @registry-atom
                            {::sec 0, ::ms 900} {::ms 300})))

    (t/is (= {::sec 30 ::min 1}
             (sut/add-delta @registry-atom
                            {::min 0, ::sec 40} {::sec 50})))

    (t/is (= {::min 30 ::hour 1}
             (sut/add-delta @registry-atom
                            {::min 40} {::min 50})))

    (t/is (= {::hour 3 ::day 1}
             (sut/add-delta @registry-atom
                            {::day 0, ::hour 13} {::hour 14})))

    (t/is (= {::year 2011 ::month :jan ::day 2 ::hour 4}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :jan ::day 1 ::hour 23} {::hour 5})))

    (t/is (= {::year 2011 ::month :feb ::day 2}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :jan ::day 30} {::day 3})))

    (t/is (= {::year 2012 ::month :jan ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :jan ::day 1} {::day 365})))

    (t/is (= {::year 2012 ::month :jan ::day 1 ::hour 4}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :dec ::day 31 ::hour 23} {::hour 5})))

    (t/is (= {::year 2010 ::month :dec ::day 31 ::hour 23}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :jan ::day 1 ::hour 0} {::hour -1})))

    (t/is (= {::year 2010 ::month :dec ::day 31 ::hour 23 ::min 59 ::sec 59}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :jan ::day 1 ::hour 0} {::sec -1})))

    (t/is (= {::year 2010 ::month :dec ::day 31 ::hour 23 ::min 59 ::sec 59 ::ms 999}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :jan ::day 1 ::hour 0} {::ms -1})))

    (t/is (= {::year 2010 ::month :dec ::day 31 ::hour 23 ::min 30}
             (sut/add-delta @registry-atom
                            {::year 2011 ::month :jan ::day 1 ::hour 23} {::hour -23 ::min -30})))

    (t/is (= {::year 2019 ::month :dec ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2019 ::month :nov ::day 1} {::month 1})))

    (t/is (= {::year 2020 ::month :jan ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2019 ::month :nov ::day 1} {::month 2})))

    (t/is (= {::year 2020 ::month :jan ::day 1}
             (sut/add-delta @registry-atom
                            {::year 2019 ::month :dec ::day 1} {::month 1})))

    (t/is (= {::year 2019 ::month :dec ::day 31}
             (sut/add-delta @registry-atom
                            {::year 2019 ::month :nov ::day 31} {::month 1})))

    (t/is (= {::year 2020 ::month :feb}
             (sut/add-delta @registry-atom
                            {::year 2020 ::month :feb} {::day 0})))

    (t/is (= {::year 2019, ::month :dec, ::day 10, ::hour 15, ::min 17, ::sec 50, ::ms 911}
             (sut/add-delta @registry-atom
                            {::year 2019, ::month :dec, ::day 10, ::hour 13, ::min 17, ::sec 50, ::ms 911} {::hour 2})))

    (t/is (= {::hour 14 :tz {::hour 2}}
             (sut/add-delta @registry-atom
                            {::hour 4 :tz {::hour 2}} {::hour 10})))

    (t/is (= {::hour 2 :tz {::hour -2}}
             (sut/add-delta @registry-atom
                            {::hour 1 :tz {::hour -2}} {::hour 1}))))

  (t/testing "-"
    (t/is (= {::year 2016, ::month :jan, ::day 1, ::hour 23, ::min 30}
             (sut/subtract-delta @registry-atom
                                 {::year 2016, ::month :dec, ::day 31, ::hour 23, ::min 30} {::day 365})))

    (t/is (= {::year 2015, ::month :dec, ::day 31, ::hour 23, ::min 30}
             (sut/subtract-delta @registry-atom
                                 {::year 2016 ::month :dec ::day 31 ::hour 23 ::min 30} {::day 366})))

    (t/is (= {::year 2020 ::month :jan ::day 31}
             (sut/subtract-delta @registry-atom
                                 {::year 2020 ::month :feb}
                                 {::day 1})))
    (t/is (= {::year 2020 ::month :feb}
             (sut/subtract-delta @registry-atom
                                 {::year 2020 ::month :feb}
                                 {::day 0})))

    (t/is (sut/eq? @registry-atom
                   {::hour 0, :tz {::hour -2}}
                   (sut/subtract-delta @registry-atom
                                       {::hour 2 :tz {::hour -2}} {::hour 2})))
    (t/is (sut/eq? @registry-atom
                   {::hour 0}
                   (sut/subtract-delta @registry-atom
                                       {::hour 2 :tz {::hour -2}} {::hour 2})))
    (t/is (sut/eq? @registry-atom
                   {::hour 2}
                   (sut/subtract-delta @registry-atom
                                       {::hour 3 :tz {::hour 2}} {::hour 1 :tz {::hour 2}}))))

  (t/testing "difference"
    (t/is (= {::day 6}
             (sut/difference @registry-atom
                             {::day 20, ::month :jul, ::year 2020}
                             {::day 26, ::month :jul, ::year 2020})))
    (t/is (= {::day 6}
             (sut/difference @registry-atom
                             {::day 26, ::month :jul, ::year 2020}
                             {::day 20, ::month :jul, ::year 2020})))
    (t/is (= {::day 22, ::year 23}
             (sut/difference @registry-atom
                             {::day 27, ::month :jul, ::year 2020}
                             {::day 5, ::month :jul, ::year 1997})))
    (t/is (= {::day 22, ::year 23}
             (sut/difference @registry-atom
                             {::day 5, ::month :jul, ::year 1997}
                             {::day 27, ::month :jul, ::year 2020})))
    (t/is (= {::year 1}
             (sut/difference @registry-atom
                             {::year 1} {::year -1})))
    (t/is (= {::year 1}
             (sut/difference @registry-atom
                             {::year -1} {::year 1})))
    (t/is (empty? (sut/difference @registry-atom
                                  {::day 27, ::month :jul, ::year 2020}
                                  {::day 27, ::month :jul, ::year 2020})))
    (t/is (= {::day 1}
             (sut/difference @registry-atom
                             {::day 28, ::month :jul, ::year 2020}
                             {::day 27, ::month :jul, ::year 2020})))
    #_(t/is (= {::hour 0}
               (sut/difference @registry-atom
                               {::hour 12, ::min 30, :tz {::hour -2}}
                               {::hour 14, ::min 30, :tz {::hour 0}})))
    (t/is (= {::year 2, ::day 5}
             (sut/difference @registry-atom
                             {::day 28, ::month :jun, ::year 2020}
                             {::day 3, ::month :jul, ::year 2022})))
    (t/is (= {::year 2, ::month 1, ::day 6}
             (sut/difference @registry-atom
                             {::day 28, ::month :may, ::year 2020}
                             {::day 3, ::month :jul, ::year 2022})))
    (t/is (= {::day 5}
             (sut/difference @registry-atom
                             {::day 28, ::month :jun, ::year 2022}
                             {::day 3, ::month :jul, ::year 2022})))
    (t/is (= {::day 2}
             (sut/difference @registry-atom
                             {::day 1, ::month :mar, ::year 2020}
                             {::day 28, ::month :feb, ::year 2020})))
    (t/is (= {::day 1}
             (sut/difference @registry-atom
                             {::day 1, ::month :mar, ::year 2021}
                             {::day 28, ::month :feb, ::year 2021})))
    (t/is (= {::year 2, ::day 2}
             (sut/difference @registry-atom
                             {::day 1, ::month :mar, ::year 2022}
                             {::day 28, ::month :feb, ::year 2020})))
    (t/is (= {::year 1, ::day 1}
             (sut/difference @registry-atom
                             {::day 1, ::month :mar, ::year 2020}
                             {::day 28, ::month :feb, ::year 2019})))
    (t/is (= {::month 11, ::day 27}
             (sut/difference @registry-atom
                             {::day 1, ::month :mar, ::year 2020}
                             {::day 28, ::month :feb, ::year 2021})))
    (t/is (= {::month 11, ::day 1}
             (sut/difference @registry-atom
                             {::day 1, ::month :mar, ::year 2021}
                             {::day 31, ::month :mar, ::year 2020})))


    (t/is (= {::month 1, ::day 3}
             (sut/difference @registry-atom
                             {::day 29, ::month :jan, ::year 2022}
                             {::day 1, ::month :mar, ::year 2022})))
    (t/is (= {::month 1, ::day 2}
             (sut/difference @registry-atom
                             {::day 30, ::month :jan, ::year 2022}
                             {::day 1, ::month :mar, ::year 2022})))
    (t/is (= {::month 1, ::day 1}
             (sut/difference @registry-atom
                             {::day 31, ::month :jan, ::year 2022}
                             {::day 1, ::month :mar, ::year 2022}))))

  #_(t/testing "difference-in"
      (t/is (= {::day 1010}
               (sut/difference-in [::ms ::day]
                                  {::year 2019, ::month :jul, ::day 28}
                                  {::year 2022, ::month :may, ::day 3})))

      (t/is (= {::year 2, ::month 9}
               (sut/difference-in [::month ::year]
                                  {::year 2019, ::month :jul, ::day 28}
                                  {::year 2022, ::month :may, ::day 3})))

      (t/is (= {::month 33}
               (sut/difference-in [::month]
                                  {::year 2019, ::month :jul, ::day 28}
                                  {::year 2022, ::month :may, ::day 3})))

      (t/is (= {::year 2, ::month 9, ::day 6}
               (sut/difference-in [::month ::day ::year]
                                  {::year 2019, ::month :jul, ::day 28}
                                  {::year 2022, ::month :may, ::day 3})))

      (t/is (= {::day 1009, ::ms 48600000}
               (sut/difference-in [::ms ::day]
                                  {::year 2019, ::month :jul, ::day 28
                                   ::hour 10, ::min 30}
                                  {::year 2022, ::month :may, ::day 3})))))


(t/deftest ^:kaocha/pending demo-test
  (sut/defseq registry-atom ::ms   #unit-map/seq[0 1 .. 999 -> ::sec])
  (sut/defseq registry-atom ::sec  #unit-map/seq[0 1 .. 59 -> ::min])
  (sut/defseq registry-atom ::min  #unit-map/seq[0 1 .. 59 -> ::hour])
  (sut/defseq registry-atom ::hour #unit-map/seq[0 1 .. 23 -> ::day])

  (sut/defseq registry-atom ::day   #unit-map/seq[1 2 .. days-in-month -> ::month])
  (sut/defseq registry-atom ::month #unit-map/seq[:jan :feb  :mar :apr :may  :jun :jul :aug  :sep :oct :nov  :dec -> ::year])
  (sut/defseq registry-atom ::year  #unit-map/seq[##-Inf .. -2 -1 1 2 .. ##Inf])

  (sut/defsys registry-atom 'ms-year    [::ms ::sec ::min ::hour ::day ::month ::year])

  #_(sut/deffmt :iso/month [::month (fn [v fmt-el] '???)])
  #_(sut/deffmt :iso/day [::day 2 "0"])

  #_"NOTE: arithmetics for now can be stubbed with simple update/inc etc"
  #_"NOTE: need some configs to map months enum to numbers"
  #_"NOTE: for sequences consisting of only static ranges calculate leading 0 padding automatically"

  (defn job-status-at [job {::keys [current-time in-fmt out-fmt]}]
    #_"TODO")

  (t/is (= (job-status-at
             {:resourceType "Job"
              :name         "denormalize"
              :start-at     {::hour 5}
              :last-run     "2022-04-01T05:00:00.000"}
             {:current-time "2022-04-01T14:30:00.000"
              :in-fmt  [::year \- :iso/month \- :iso/day \T ::hour \: ::min \: ::sec \. ::ms]
              :out-fmt [::year \- :iso/month \- :iso/day \T ::hour \: ::min \: ::sec \. ::ms]})
           {:latst-run           "2022-04-01T05:00:00.000"
            :next-run            "2022-04-02T05:00:00.000"
            :should-start-now?   false
            :time-until-next-run {::hour 14, ::min 30}})))
