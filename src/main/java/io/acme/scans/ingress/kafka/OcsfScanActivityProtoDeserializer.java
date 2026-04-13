package io.acme.scans.ingress.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import io.acme.scans.ingress.ocsf.proto.ScanActivity;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * Kafka value deserializer for binary OCSF {@link ScanActivity} protobuf payloads.
 */
public class OcsfScanActivityProtoDeserializer implements Deserializer<ScanActivity> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // no-op
    }

    @Override
    public ScanActivity deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return ScanActivity.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new SerializationException("Invalid OCSF ScanActivity protobuf for topic=" + topic, e);
        }
    }

    @Override
    public void close() {
        // no-op
    }
}
