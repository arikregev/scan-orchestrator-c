package io.acme.scans.temporal.activity;

import io.acme.scans.domain.ScanRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface(namePrefix = "ScaManifest_")
public interface ScaManifestScanActivities {
    @ActivityMethod
    String submit(ScanRequest request);

    @ActivityMethod
    String getStatus(String scanId);
}

