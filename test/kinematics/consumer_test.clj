(ns kinematics.consumer-test
  (:require [clojure.test :refer :all]
            [kinematics.consumer :as consume]))


(deftest config-test
  (is (= "waffle" (.getApplicationName (consume/config "waffle" "x" {}))))
  (is (= "x" (.getStreamName (consume/config "waffle" "x" {}))))
  (is (= 50000 (.getFailoverTimeMillis (consume/config "waffle" "x" {:failover-time-millis 50000}))))
  (is (= "us-east-1" (.getRegionName (consume/config "waffle" "x" {:region "us-east-1"})))))
