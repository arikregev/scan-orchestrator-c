package io.acme.scans.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScanRequest(
        @NotNull Integer appId,
        @NotBlank String componentName,
        @NotBlank String buildId,
        @NotNull ScanTool tool,
        String repoUrl,
        String commitSha,
        String branchName,
        String sourceUrl,
        String originalEventUid
) {
}

