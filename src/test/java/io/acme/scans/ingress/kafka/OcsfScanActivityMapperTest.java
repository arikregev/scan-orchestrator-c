package io.acme.scans.ingress.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.acme.scans.domain.ScanTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OcsfScanActivityMapperTest {

    @Test
    void mapsFieldsFromExampleShape() {
        String json = """
                {
                  "metadata": { "original_event_uid": "evt-123" },
                  "tags": [
                    { "name": "app_id", "value": "17045" },
                    { "name": "component_name", "value": "var-service" }
                  ],
                  "scan": { "type": "SAST" },
                  "fields": [
                    { "key": "branch_name", "value": { "string_value": "main" } },
                    { "key": "build_id", "value": { "string_value": "69662bffca01a04b3436f2e0" } },
                    { "key": "code_repository_url", "value": { "string_value": "https://github.com/company/my-application.git" } },
                    { "key": "git_commit_id", "value": { "string_value": "a1b2c3d4e5" } },
                    { "key": "source_url", "value": { "string_value": "http://example.local/source.zip" } }
                  ]
                }
                """;

        OcsfScanActivityMapper mapper = new OcsfScanActivityMapper(new ObjectMapper());
        var req = mapper.fromJson(json);

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
}

