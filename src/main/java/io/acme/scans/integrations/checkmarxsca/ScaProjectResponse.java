package io.acme.scans.integrations.checkmarxsca;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ScaProjectResponse(
        String id,
        String name,
        @JsonProperty("assignedTeams") List<String> assignedTeams
) {
}

