package io.acme.scans.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "temporal")
public interface TemporalConfig {
    String target();
    String namespace();
    String taskQueue();
}

