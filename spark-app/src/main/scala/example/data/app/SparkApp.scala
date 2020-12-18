package example.data.app

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}

object SparkApp extends BaseApp {

  def process(): Unit = {
    val mainDf = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaHost.mkString(","))
      .option("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      .option("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
      .option("subscribe", appConfig.kafkaTopic.mkString(","))
      .option("startingOffsets", appConfig.startingOffsets)
      .load()

    mainDf.writeStream.foreachBatch((batchDS: DataFrame, batchId: Long) => {
      batchDS.show()
    })
      .trigger(Trigger.ProcessingTime(1 * 1000L))
      .outputMode(OutputMode.Append())
      .option("checkpointLocation", appConfig.checkpoint)
      .start().awaitTermination()
  }
}
