package io.acme.scans.integrations.checkmarxone.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CxOneProjectLookupResponse(
        @JsonProperty("projects") List<Project> projects
) {
    public record Project(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name
    ) {
    }
}

