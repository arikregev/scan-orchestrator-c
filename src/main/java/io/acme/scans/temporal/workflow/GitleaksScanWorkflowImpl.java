package io.acme.scans.temporal.workflow;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanResult;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.temporal.activity.GitleaksScanActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class GitleaksScanWorkflowImpl implements GitleaksScanWorkflow {
    private final GitleaksScanActivities activities = Workflow.newActivityStub(
            GitleaksScanActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .build()
    );

    @Override
    public ScanResult run(ScanRequest request) {
        String scanId = activities.submit(request);
        String status = activities.getStatus(scanId);
        return new ScanResult(ScanTool.GITLEAKS, scanId, status);
    }
}
