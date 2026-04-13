package io.acme.scans.ingress.kafka;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScanToolRouter {
    public ScanTool resolveTool(ScanRequest request) {
        if (request == null || request.tool() == null) {
            throw new IllegalArgumentException("Missing tool");
        }
        // Phase 1 supported tools
        if (request.tool() == ScanTool.SAST
                || request.tool() == ScanTool.SCA_MANIFEST
                || request.tool() == ScanTool.GITLEAKS) {
            return request.tool();
        }
        throw new IllegalArgumentException("Unsupported tool (phase 1): " + request.tool());
    }
}

