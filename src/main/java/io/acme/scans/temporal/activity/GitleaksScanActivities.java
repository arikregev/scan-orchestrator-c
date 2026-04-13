package io.acme.scans.temporal.activity;

import io.acme.scans.domain.ScanRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface(namePrefix = "GitleaksScan_")
public interface GitleaksScanActivities {
    @ActivityMethod
    String submit(ScanRequest request);

    @ActivityMethod
    String getStatus(String scanId);
}
