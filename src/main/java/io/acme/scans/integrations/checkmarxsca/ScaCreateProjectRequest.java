package io.acme.scans.integrations.checkmarxsca;

import java.util.List;

public record ScaCreateProjectRequest(
        String name,
        List<String> assignedTeams
) {
}

