package app

import io.circe.Error
import org.apache.spark.sql.SparkSession
import java.io.{File, FileInputStream, InputStreamReader}

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.yaml

trait BaseApp {
  var sparksession:SparkSession = null
  var appConfig: ConfigLoader.App = null
  var kafkaHost = List[String]()
  def process()

  def main(args: Array[String]) {
    if(args.length==1){
      System.exit(1)
    }
    val configPath = args(0)
    val appName = args(1)
    sparksession = SparkSession.builder().appName(appName)
      .getOrCreate()

    val config = ConfigLoader.load(configPath)
    println(config)
    val appConfigs = config.jobs.filter(_.job.equals(appName)).take(1)
    kafkaHost = config.kafkahosts
    appConfig = appConfigs.length match {
      case 1 => appConfigs(0)
      case _ => null
    }
    process()
  }

}

  object ConfigLoader {
    case class Config(kafkahosts: List[String],
                      jobs: List[App])
    case class App(job:String,
                   checkpoint: String,
                   kafkaTopic: List[String],
                   startingOffsets: String)

    def load(path: String): Config ={
      val config = new FileInputStream(path);;
      val yml = yaml.parser.parse(new InputStreamReader(config))

      yml.leftMap(err => err: Error)
        .flatMap(_.as[Config])
        .valueOr(throw _)
    }
}

