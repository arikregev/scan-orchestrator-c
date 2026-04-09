package io.acme.scans.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "checkmarx-sca")
public interface CheckmarxScaConfig {
    /**
     * Base URL for SCA REST API, e.g. https://api-sca.checkmarx.net (US) or https://eu.api-sca.checkmarx.net (EU).
     */
    @WithName("base-url")
    String baseUrl();

    /**
     * Prefix used for assignedTeams entries (docs show values like "/CxServer/Team03").
     */
    @WithName("team-prefix")
    @WithDefault("/CxServer/")
    String teamPrefix();
}

