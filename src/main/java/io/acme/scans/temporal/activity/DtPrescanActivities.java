package io.acme.scans.temporal.activity;

import io.acme.scans.domain.ScanRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DtPrescanActivities {
    @ActivityMethod
    byte[] fetchSbom(ScanRequest request);

    @ActivityMethod
    void publishToDependencyTrack(String projectName, String projectVersion, byte[] sbomJsonBytes);
}

