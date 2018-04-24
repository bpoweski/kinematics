(ns kinematics.consumer
  (:import (com.amazonaws.services.kinesis.clientlibrary.interfaces IRecordProcessorCheckpointer)
           (com.amazonaws.services.kinesis.clientlibrary.interfaces.v2 IRecordProcessor IRecordProcessorFactory)
           (com.amazonaws.services.kinesis.clientlibrary.lib.worker KinesisClientLibConfiguration Worker Worker$Builder)))


(defn worker-id
  ([] (worker-id (str (java.net.InetAddress/getLocalHost))))
  ([ip-address]
   (str ip-address ":" (java.util.UUID/randomUUID))))

(defn config [app-name stream {:keys [credential-provider id] :as opts :or {credential-provider (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.) id (worker-id)}}]
  (let [{:keys [failover-time-millis region]} opts]
    (cond-> (KinesisClientLibConfiguration. app-name stream credential-provider id)
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
