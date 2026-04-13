package io.acme.scans.temporal.activity;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.integrations.checkmarxone.CheckmarxOneClient;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GitleaksScanActivitiesImpl implements GitleaksScanActivities {
    private final CheckmarxOneClient checkmarxOneClient;

    public GitleaksScanActivitiesImpl(CheckmarxOneClient checkmarxOneClient) {
        this.checkmarxOneClient = checkmarxOneClient;
    }

    @Override
    public String submit(ScanRequest request) {
        return checkmarxOneClient.submitScan(ScanTool.GITLEAKS, request);
    }

    @Override
    public String getStatus(String scanId) {
        return checkmarxOneClient.getScanStatus(ScanTool.GITLEAKS, scanId);
    }
}
