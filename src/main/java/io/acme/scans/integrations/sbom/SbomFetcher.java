package io.acme.scans.integrations.sbom;

import io.acme.scans.domain.ScanRequest;

public interface SbomFetcher {
    byte[] fetchSbom(ScanRequest request);
}

