package io.acme.scans.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "dependency-track")
public interface DependencyTrackConfig {
    @WithName("base-url")
    String baseUrl();

    @WithName("api-key")
    String apiKey();
}

