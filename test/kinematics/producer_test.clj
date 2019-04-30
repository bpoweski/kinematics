(ns kinematics.producer-test
  (:require  [clojure.test :refer :all]
             [kinematics.producer :as pro]))


(deftest config-test
  (is (= (.getRecordMaxBufferedTime (pro/config {:record-max-buffered-time 1000})) 1000))
  (is (= (.getRegion (pro/config {:region :us-east-1 })) "us-east-1"))
  (is (= (.getRegion (pro/config {:region "us-east-1"})) "us-east-1"))
  (is (false? (.isAggregationEnabled (pro/config {:aggregation-enabled false}))))
  (is (true? (.isAggregationEnabled (pro/config {:aggregation-enabled true})))))
