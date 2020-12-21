package example.trap.protobuf.producer;

import example.trap.serializer.SimpleMessageSerializer;
import example.trap.protobuf.SimpleMessageProtos.SimpleMessage;

import com.google.protobuf.Timestamp;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class ProtobufProducer {

    public static void main(String[] args) throws InterruptedException {
        ProtobufProducer protobufProducer = new ProtobufProducer();
        protobufProducer.writeMessage();
    }

    public void writeMessage() throws InterruptedException {
        //create kafka producer
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SimpleMessageSerializer.class);
        // properties.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        Producer<String, SimpleMessage> producer = new KafkaProducer<>(properties);

        int counter = 0;
        while (counter < 10000) {

            Instant time = Instant.now();
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(time.getEpochSecond())
                .setNanos(time.getNano()).build();

            //prepare the message
            counter++;
            SimpleMessage simpleMessage =
                SimpleMessage.newBuilder()
                    .setId(counter)
                    .setContent("Hello world " + counter)
                    .setStringDateTime(Instant.now().toString())
                    .setProtoDataTime(timestamp)
                    .setChangesContent("Changes " + counter)
                    .build();

            System.out.println(simpleMessage);

            //prepare the kafka record
            ProducerRecord<String, SimpleMessage> record
                = new ProducerRecord<>("simplemessage", null, simpleMessage);

            producer.send(record);

            sleep(10);
            //ensures record is sent before closing the producer

        }

        producer.flush();

        producer.close();
    }

}
