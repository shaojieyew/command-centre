package app

object SparkApp extends BaseApp {

  def process(): Unit = {
    val spark = sparksession
    import spark.implicits._
    while(true){
      val df = Seq(appConfig.job,appConfig.kafkaTopic.mkString(","),appConfig.checkpoint).toDF
      df.show()
    }
  }

}
