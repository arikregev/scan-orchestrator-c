package io.acme.scans.ingress.kafka;

import io.acme.scans.ingress.ocsf.proto.Metadata;
import io.acme.scans.ingress.ocsf.proto.ScanActivity;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OcsfScanActivityProtoDeserializerTest {

    @Test
    void deserializesRoundTrip() {
        ScanActivity original = ScanActivity.newBuilder()
                .setMetadata(Metadata.newBuilder().setOriginalEventUid("uid-1").build())
                .build();
        byte[] bytes = original.toByteArray();

        var deser = new OcsfScanActivityProtoDeserializer();
        ScanActivity decoded = deser.deserialize("ocsf.scan.activity", bytes);

        assertEquals(original, decoded);
        assertEquals("uid-1", decoded.getMetadata().getOriginalEventUid());
    }

    @Test
    void nullOrEmptyReturnsNull() {
        var deser = new OcsfScanActivityProtoDeserializer();
        assertNull(deser.deserialize("t", null));
        assertNull(deser.deserialize("t", new byte[0]));
    }

    @Test
    void corruptPayloadThrowsSerializationException() {
        var deser = new OcsfScanActivityProtoDeserializer();
        assertThrows(SerializationException.class, () -> deser.deserialize("t", new byte[] { 0x0f, 0x1e, 0x2d }));
    }
}
