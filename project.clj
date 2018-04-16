(defproject kinematics "0.1.0"
  :description "AWS Kinesis Consumer & Producer"
  :url "http://github.com/bpoweski/kinematics"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.amazonaws/amazon-kinesis-client "1.9.0"]
                 [com.amazonaws/amazon-kinesis-producer "0.12.7"]]) ;; 0.12.8 has a spin lock bug, 0.12.9 not pushed to mvn
