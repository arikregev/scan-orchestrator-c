package io.acme.scans.integrations.dependencytrack;

public interface DependencyTrackBomPublisher {
    void publish(String projectName, String projectVersion, byte[] sbomJsonBytes);
}

