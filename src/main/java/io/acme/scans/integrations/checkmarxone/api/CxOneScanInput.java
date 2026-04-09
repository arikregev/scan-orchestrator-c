package io.acme.scans.integrations.checkmarxone.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * This shape is based on public examples for Checkmarx One scan creation for an uploaded zip.
 * If your tenant expects different fields, adjust here once you capture an actual request from the CLI/UI.
 */
public record CxOneScanInput(
        @JsonProperty("projectId") String projectId,
        @JsonProperty("branch") String branch,
        @JsonProperty("sourceType") String sourceType,
        @JsonProperty("handler") Handler handler,
        @JsonProperty("config") List<ConfigItem> config
) {
    public record Handler(@JsonProperty("uploadUrl") String uploadUrl) {
    }

    public record ConfigItem(@JsonProperty("type") String type,
                             @JsonProperty("value") Map<String, Object> value) {
    }
}

