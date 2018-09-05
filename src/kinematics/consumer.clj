(ns kinematics.consumer
  (:require [clojure.tools.logging :as log])
  (:import (com.amazonaws.services.kinesis.clientlibrary.interfaces IRecordProcessorCheckpointer)
           (com.amazonaws.services.kinesis.clientlibrary.interfaces.v2 IRecordProcessor IRecordProcessorFactory)
           (com.amazonaws.services.kinesis.clientlibrary.lib.worker KinesisClientLibConfiguration Worker Worker$Builder)
           (com.amazonaws.services.kinesis.clientlibrary.exceptions ShutdownException ThrottlingException InvalidStateException)))


(defn worker-id
  ([] (worker-id (str (java.net.InetAddress/getLocalHost))))
  ([ip-address]
   (str ip-address ":" (java.util.UUID/randomUUID))))

(def checkpoint-retries 10)

(defn exp-wait
  "Sleeps according to exp-wait-ms"
  [n base max-wait]
  (Thread/sleep (min max-wait (* (Math/pow 2 n) base))))

(defn checkpoint!
  "Checkpoint logic modeled after https://github.com/aws/aws-sdk-java/blob/master/src/samples/AmazonKinesis/AmazonKinesisApplicationSampleRecordProcessor.java"
  [^IRecordProcessorCheckpointer checkpointer]
  (loop [attempts 0]
    (if (< attempts checkpoint-retries)
      (let [outcome (try
                      (.checkpoint checkpointer)
                      :ok
                      (catch ShutdownException ex
                        (log/warn "caught shutdown exception on checkpoint, skipping")
                        :ok)
                      (catch ThrottlingException ex
                        :retry)
                      (catch IllegalStateException ex
                        (log/warn ex "cannot save checkpoint")
                        :error))]
        (when (= outcome :retry)
          (exp-wait attempts 300 30000)
          (recur (inc attempts))))
      (log/warn "checkpointing failed after" attempts))))

(defn config [app-name stream {:keys [kinesis-credential-provider dynamodb-credential-provider cloudwatch-credential-provider credential-provider id] :as opts}]
  (let [{:keys [failover-time-millis region]} opts
        id                                    (or id (worker-id))
        credential-provider                   (or credential-provider (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
        kinesis-credential-provider           (or kinesis-credential-provider credential-provider)
        dynamodb-credential-provider          (or dynamodb-credential-provider credential-provider)
        cloudwatch-credential-provider        (or cloudwatch-credential-provider credential-provider)]
    (cond-> (KinesisClientLibConfiguration. app-name stream kinesis-credential-provider dynamodb-credential-provider cloudwatch-credential-provider id)
      (number? failover-time-millis) (.withFailoverTimeMillis (long failover-time-millis))
      (or (string? region) (keyword? region)) (.withRegionName (name region)))))

(defn worker
  "Creates a kinesis worker and IRecordProcessorFactory instance"
  [handler ^String app-name ^String stream {:keys [init-fn shutdown-fn credential-provider] :as opts}]
  (let [factory (reify IRecordProcessorFactory
                  (createProcessor [_]
                    (reify IRecordProcessor
                      (initialize [_ input]
                        (when (fn? init-fn)
                          (init-fn input)))
                      (processRecords [_ input]
                        (handler input))
                      (shutdown [_ shutdown-input]
                        (when (fn? shutdown-fn)
                          (shutdown-fn shutdown-input))))))]
    (-> (Worker$Builder.)
        (.recordProcessorFactory factory)
        (.config (config app-name stream opts))
        (.build))))

(defn start! [^Worker worker]
  (doto (Thread. worker)
    (.start))
  worker)

(defn stop! [^Worker worker]
  (when (instance? Worker worker)
    @(.startGracefulShutdown worker))
  worker)
