

import example.trap.protobuf.SimpleMessageProtos.SimpleMessage;
import com.google.protobuf.Timestamp;

import java.time.Instant;


public class ExampleProtobuf {

    public static void main(String... args) {

        Instant time = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(time.getEpochSecond())
                .setNanos(time.getNano()).build();

        //prepare the message
        int counter = 0;

        SimpleMessage simpleMessage =
                SimpleMessage.newBuilder()
                        .setId(counter)
                        .setContent("Hello world " + counter)
                        .setStringDateTime(Instant.now().toString())
                        .setProtoDataTime(timestamp)
                        .setChangesContent("Changes " + counter)
                        .build();

        System.out.println(simpleMessage);
    }
}