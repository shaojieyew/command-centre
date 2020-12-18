package example.data.generator;

object Main {
    def main(arg:Array[String]): Unit ={
      val kafkaHost = "localhost"
      val kafkaPort = "9092"
      val topic = "test"
      val generator = new PersonGenerator(List(
        ConsoleWriter,
        new KafkaWriter().setHost(kafkaHost).setPort(kafkaPort).setTopic(topic))
      )
      generator.writeForever(1, 2)
    }
}
