package io.acme.scans.temporal.workflow;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.temporal.activity.DtPrescanActivities;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtPrescanWorkflowUnitTest {
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
    void publishesWithExpectedProjectMapping() {
        AtomicReference<String> publishedName = new AtomicReference<>();
        AtomicReference<String> publishedVersion = new AtomicReference<>();

        Worker worker = env.newWorker("dt-queue");
        worker.registerWorkflowImplementationTypes(DtPrescanWorkflowImpl.class);
        worker.registerActivitiesImplementations(new DtPrescanActivities() {
            @Override
            public byte[] fetchSbom(ScanRequest request) {
                return "{}".getBytes();
            }

            @Override
            public void publishToDependencyTrack(String projectName, String projectVersion, byte[] sbomJsonBytes) {
                publishedName.set(projectName);
                publishedVersion.set(projectVersion);
            }
        });
        env.start();

        var wf = env.getWorkflowClient().newWorkflowStub(
                DtPrescanWorkflow.class,
                io.temporal.client.WorkflowOptions.newBuilder().setTaskQueue("dt-queue").build()
        );

        wf.run(new ScanRequest(17045, "var-service", "b1", ScanTool.SCA_MANIFEST, null, null, null, null, "evt"));

        assertEquals("17045:var-service", publishedName.get());
        assertEquals("b1", publishedVersion.get());
    }
}

