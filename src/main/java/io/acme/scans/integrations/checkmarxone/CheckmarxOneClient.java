package io.acme.scans.integrations.checkmarxone;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;

public interface CheckmarxOneClient {
    String submitScan(ScanTool tool, ScanRequest request);

    String getScanStatus(ScanTool tool, String scanId);
}

