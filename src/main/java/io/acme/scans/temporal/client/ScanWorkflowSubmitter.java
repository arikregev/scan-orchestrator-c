package io.acme.scans.temporal.client;

import io.acme.scans.config.TemporalConfig;
import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.temporal.workflow.SastScanWorkflow;
import io.acme.scans.temporal.workflow.ScaManifestScanWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScanWorkflowSubmitter {
    private final WorkflowClient workflowClient;
    private final TemporalConfig temporalConfig;

    public ScanWorkflowSubmitter(WorkflowClient workflowClient, TemporalConfig temporalConfig) {
        this.workflowClient = workflowClient;
        this.temporalConfig = temporalConfig;
    }

    public void submit(ScanRequest request) {
        String workflowId = WorkflowIds.forRequest(request);
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(temporalConfig.taskQueue())
                .setWorkflowId(workflowId)
                .build();

        if (request.tool() == ScanTool.SAST) {
            SastScanWorkflow wf = workflowClient.newWorkflowStub(SastScanWorkflow.class, options);
            WorkflowClient.start(wf::run, request);
            return;
        }
        if (request.tool() == ScanTool.SCA_MANIFEST) {
            ScaManifestScanWorkflow wf = workflowClient.newWorkflowStub(ScaManifestScanWorkflow.class, options);
            WorkflowClient.start(wf::run, request);
            return;
        }
        throw new IllegalArgumentException("Unsupported tool (phase 1): " + request.tool());
    }
}

