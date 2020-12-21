package example.trap.serializer;

import com.google.protobuf.InvalidProtocolBufferException;
import example.trap.protobuf.SimpleMessageProtos.SimpleMessage;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMessageDeserializer extends Adapter implements Deserializer<SimpleMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleMessageDeserializer.class);

    @Override
    public SimpleMessage deserialize(final String topic, byte[] data) {
        try {
            return SimpleMessage.parseFrom(data);
        } catch (final InvalidProtocolBufferException e) {
            LOG.error("Received unparseable message", e);
            throw new RuntimeException("Received unparseable message " + e.getMessage(), e);
        }
    }

}