package io.acme.scans.temporal.workflow;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.temporal.activity.DtPrescanActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DtPrescanWorkflowImpl implements DtPrescanWorkflow {
    private final DtPrescanActivities activities = Workflow.newActivityStub(
            DtPrescanActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(5))
                    .build()
    );

    @Override
    public void run(ScanRequest request) {
        byte[] sbom = activities.fetchSbom(request);
        String projectName = "%d:%s".formatted(request.appId(), request.componentName());
        String projectVersion = request.buildId();
        activities.publishToDependencyTrack(projectName, projectVersion, sbom);
    }
}

