package io.acme.scans.ingress.kafka;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.ingress.ocsf.proto.KeyValueObject;
import io.acme.scans.ingress.ocsf.proto.Metadata;
import io.acme.scans.ingress.ocsf.proto.Product;
import io.acme.scans.ingress.ocsf.proto.Scan;
import io.acme.scans.ingress.ocsf.proto.ScanActivity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OcsfScanActivityMapperTest {

    @Test
    void mapsFromProtobufCheckmarxSampleShape() {
        ScanActivity msg = ScanActivity.newBuilder()
                .setMetadata(Metadata.newBuilder()
                        .setOriginalEventUid("evt-123")
                        .setProduct(Product.newBuilder().setName("CHECKMARX").build())
                        .addTags(KeyValueObject.newBuilder().setName("app_id").setValue("17045").build())
                        .addTags(KeyValueObject.newBuilder().setName("component_name").setValue("var-service").build())
                        .setVersion("1.2.0")
                        .build())
                .setScan(Scan.newBuilder().setType("SAST").setUid("scan-uid").setTypeId("SCAN_TYPE_ID_OTHER").build())
                .setUnmapped(Struct.newBuilder()
                        .putFields("branch_name", Value.newBuilder().setStringValue("main").build())
                        .putFields("build_id", Value.newBuilder().setStringValue("69662bffca01a04b3436f2e0").build())
                        .putFields("code_repository_url", Value.newBuilder()
                                .setStringValue("https://github.com/company/my-application.git").build())
                        .putFields("git_commit_id", Value.newBuilder().setStringValue("a1b2c3d4e5").build())
                        .putFields("source_url", Value.newBuilder().setStringValue("http://example.local/source.zip").build())
                        .build())
                .build();

        OcsfScanActivityMapper mapper = new OcsfScanActivityMapper();
        var req = mapper.fromProto(msg);

        assertEquals(17045, req.appId());
        assertEquals("var-service", req.componentName());
        assertEquals("69662bffca01a04b3436f2e0", req.buildId());
        assertEquals(ScanTool.SAST, req.tool());
        assertEquals("https://github.com/company/my-application.git", req.repoUrl());
        assertEquals("a1b2c3d4e5", req.commitSha());
        assertEquals("main", req.branchName());
        assertEquals("http://example.local/source.zip", req.sourceUrl());
        assertEquals("evt-123", req.originalEventUid());
    }

    @Test
    void mapsGitleaksSampleViaProductWhenScanTypeUnknown() {
        ScanActivity msg = ScanActivity.newBuilder()
                .setMetadata(Metadata.newBuilder()
                        .setOriginalEventUid("https://github.com/company/asrb.git:main:SECRETS")
                        .setProduct(Product.newBuilder().setName("GITLEAKS").build())
                        .addTags(KeyValueObject.newBuilder().setName("app_id").setValue("99").build())
                        .addTags(KeyValueObject.newBuilder().setName("component_name").setValue("asrb").build())
                        .setVersion("1.7.0")
                        .build())
                .setScan(Scan.newBuilder().setType("SECRET").setUid("c3a79d29-87ac-40e9-bcae-e488f4afbdfb").build())
                .setUnmapped(Struct.newBuilder()
                        .putFields("branch_name", Value.newBuilder().setStringValue("main").build())
                        .putFields("code_repository_url", Value.newBuilder()
                                .setStringValue("https://github.com/company/asrb.git").build())
                        .putFields("request_type", Value.newBuilder().setStringValue("SCAN").build())
                        .putFields("build_id", Value.newBuilder().setStringValue("build-from-unmapped").build())
                        .build())
                .build();

        OcsfScanActivityMapper mapper = new OcsfScanActivityMapper();
        var req = mapper.fromProto(msg);

        assertEquals(99, req.appId());
        assertEquals("asrb", req.componentName());
        assertEquals(ScanTool.GITLEAKS, req.tool());
        assertEquals("https://github.com/company/asrb.git", req.repoUrl());
        assertEquals("main", req.branchName());
        assertEquals("build-from-unmapped", req.buildId());
    }
}
