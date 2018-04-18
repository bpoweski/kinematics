(ns kinematics.producer
  (:import [com.amazonaws.services.kinesis.producer KinesisProducer KinesisProducerConfiguration]
           [java.nio ByteBuffer]))


(defn ^KinesisProducerConfiguration config [{:keys [credentials-provider region max-connections request-timeout record-max-buffered-time]}]
  (cond-> (KinesisProducerConfiguration.)
    credentials-provider     (.setCredentialsProvider credentials-provider)
    region                   (.setRegion (name region))
    max-connections          (.setMaxConnections max-connections)
    request-timeout          (.setRequestTimeout request-timeout)
    record-max-buffered-time (.setRecordMaxBufferedTime record-max-buffered-time)))

(defn create [opts]
  (KinesisProducer. (config opts)))

(defn put-string [^KinesisProducer producer ^String stream ^String partition-key ^String x & {:keys [encoding] :as ops :or {encoding "UTF-8"}}]
  (.addUserRecord producer stream partition-key (ByteBuffer/wrap (.getBytes x encoding))))

(defn stop! [^KinesisProducer producer]
  (when (instance? KinesisProducer producer)
    (.flushSync producer)
    (.destroy producer)))
