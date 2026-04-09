package io.acme.scans.temporal.activity;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.integrations.checkmarxone.CheckmarxOneClient;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScaManifestScanActivitiesImpl implements ScaManifestScanActivities {
    private final CheckmarxOneClient checkmarxOneClient;

    public ScaManifestScanActivitiesImpl(CheckmarxOneClient checkmarxOneClient) {
        this.checkmarxOneClient = checkmarxOneClient;
    }

    @Override
    public String submit(ScanRequest request) {
        return checkmarxOneClient.submitScan(ScanTool.SCA_MANIFEST, request);
    }

    @Override
    public String getStatus(String scanId) {
        return checkmarxOneClient.getScanStatus(ScanTool.SCA_MANIFEST, scanId);
    }
}

