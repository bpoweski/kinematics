(defproject kinematics "0.2.1-SNAPSHOT"
  :description "AWS Kinesis Consumer & Producer"
  :url "http://github.com/bpoweski/kinematics"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.amazonaws/aws-java-sdk-core "1.11.308"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.308"]
                 [com.amazonaws/aws-java-sdk-cloudwatch "1.11.308"]
                 [com.amazonaws/aws-java-sdk-kinesis "1.11.308"]
                 [com.amazonaws/amazon-kinesis-client "1.9.0" :exclusions [aws-java-sdk]]
                 [com.amazonaws/amazon-kinesis-producer "0.12.9"] ;; 0.12.8 has a spin lock bug, 0.12.9 not pushed to mvn
                 [org.clojure/tools.logging "0.5.0-alpha"]])
