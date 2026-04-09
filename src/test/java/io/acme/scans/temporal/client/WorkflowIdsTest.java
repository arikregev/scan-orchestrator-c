package io.acme.scans.temporal.client;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowIdsTest {
    @Test
    void stableWorkflowId() {
        ScanRequest req = new ScanRequest(17045, "var-service", "build 1", ScanTool.SAST, null, null, null, null, "evt");
        assertEquals("scan:SAST:17045:var-service:build%201", WorkflowIds.forRequest(req));
    }
}

