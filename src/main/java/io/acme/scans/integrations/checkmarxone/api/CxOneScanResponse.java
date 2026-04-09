package io.acme.scans.integrations.checkmarxone.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CxOneScanResponse(
        @JsonProperty("id") String id,
        @JsonProperty("status") String status
) {
}

