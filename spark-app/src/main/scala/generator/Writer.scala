package generator

import java.util.Properties

import util.JsonUtil
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

abstract class Writer {
  def start[T](t:T): Unit
}

object ConsoleWriter extends Writer {
  override def start[T](t:T): Unit = {
    val json = JsonUtil.toJson(t)
    println(json)
  }
}

class KafkaWriter (var host: String = "localhost" ,
                   var port: String = "9092" ,
                   var topic: String = "test" ) extends Writer {

  def setHost(h:String): KafkaWriter = {
    this.host = h;
    this
  }
  def setPort(p:String): KafkaWriter = {
    this.port = p;
    this
  }
  def setTopic(t:String): KafkaWriter = {
    this.topic = t;
    this
  }

  override def start[T](t:T): Unit = {
    val json = JsonUtil.toJson(t)
    val props = new Properties()
    props.put("bootstrap.servers", "%s:%s".format(host,port))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")

    val producer = new KafkaProducer[String, Array[Byte]](props)
    val record = new ProducerRecord[String, Array[Byte]](topic, t.hashCode().toString, json.getBytes)

    producer.send(record)
    producer.close()
  }
}