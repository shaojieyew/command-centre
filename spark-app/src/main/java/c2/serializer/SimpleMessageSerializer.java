package c2.serializer;

import example.trap.protobuf.SimpleMessageProtos.SimpleMessage;
import org.apache.kafka.common.serialization.Serializer;

public class SimpleMessageSerializer extends Adapter implements Serializer<SimpleMessage> {
    @Override
    public byte[] serialize(final String topic, final SimpleMessage data) {
        return data.toByteArray();
    }
}