package io.acme.scans.temporal.workflow;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.temporal.activity.SastScanActivities;
import io.acme.scans.temporal.activity.ScaManifestScanActivities;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowUnitTest {
    private TestWorkflowEnvironment env;

    @BeforeEach
    void setUp() {
        env = TestWorkflowEnvironment.newInstance();
    }

    @AfterEach
    void tearDown() {
        env.close();
    }

    @Test
    void sastWorkflowCallsActivities() {
        Worker worker = env.newWorker("test-queue");
        worker.registerWorkflowImplementationTypes(SastScanWorkflowImpl.class);
        worker.registerActivitiesImplementations(new SastScanActivities() {
            @Override
            public String submit(ScanRequest request) {
                return "sast-1";
            }

            @Override
            public String getStatus(String scanId) {
                return "SUBMITTED";
            }
        });
        env.start();

        var client = env.getWorkflowClient();
        var wf = client.newWorkflowStub(SastScanWorkflow.class,
                io.temporal.client.WorkflowOptions.newBuilder().setTaskQueue("test-queue").build());

        var result = wf.run(new ScanRequest(1, "comp", "build", ScanTool.SAST, null, null, null, null, "evt"));
        assertEquals(ScanTool.SAST, result.tool());
        assertEquals("sast-1", result.scanId());
        assertEquals("SUBMITTED", result.status());
    }

    @Test
    void scaManifestWorkflowCallsActivities() {
        Worker worker = env.newWorker("test-queue-2");
        worker.registerWorkflowImplementationTypes(ScaManifestScanWorkflowImpl.class);
        worker.registerActivitiesImplementations(new ScaManifestScanActivities() {
            @Override
            public String submit(ScanRequest request) {
                return "sca-1";
            }

            @Override
            public String getStatus(String scanId) {
                return "SUBMITTED";
            }
        });
        env.start();

        var client = env.getWorkflowClient();
        var wf = client.newWorkflowStub(ScaManifestScanWorkflow.class,
                io.temporal.client.WorkflowOptions.newBuilder().setTaskQueue("test-queue-2").build());

        var result = wf.run(new ScanRequest(1, "comp", "build", ScanTool.SCA_MANIFEST, null, null, null, null, "evt"));
        assertEquals(ScanTool.SCA_MANIFEST, result.tool());
        assertEquals("sca-1", result.scanId());
        assertEquals("SUBMITTED", result.status());
    }
}

