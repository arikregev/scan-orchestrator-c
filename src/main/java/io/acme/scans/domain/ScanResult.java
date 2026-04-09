package io.acme.scans.domain;

public record ScanResult(
        ScanTool tool,
        String scanId,
        String status
) {
}

