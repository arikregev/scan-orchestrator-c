package io.acme.scans.temporal.workflow;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanResult;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.temporal.activity.ScaManifestScanActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class ScaManifestScanWorkflowImpl implements ScaManifestScanWorkflow {
    private final ScaManifestScanActivities activities = Workflow.newActivityStub(
            ScaManifestScanActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .build()
    );

    @Override
    public ScanResult run(ScanRequest request) {
        String scanId = activities.submit(request);
        String status = activities.getStatus(scanId);
        return new ScanResult(ScanTool.SCA_MANIFEST, scanId, status);
    }
}

