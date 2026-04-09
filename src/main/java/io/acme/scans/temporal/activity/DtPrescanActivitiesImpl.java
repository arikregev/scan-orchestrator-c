package io.acme.scans.temporal.activity;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.integrations.dependencytrack.DependencyTrackBomPublisher;
import io.acme.scans.integrations.sbom.SbomFetcher;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DtPrescanActivitiesImpl implements DtPrescanActivities {
    private final SbomFetcher sbomFetcher;
    private final DependencyTrackBomPublisher bomPublisher;

    public DtPrescanActivitiesImpl(SbomFetcher sbomFetcher, DependencyTrackBomPublisher bomPublisher) {
        this.sbomFetcher = sbomFetcher;
        this.bomPublisher = bomPublisher;
    }

    @Override
    public byte[] fetchSbom(ScanRequest request) {
        return sbomFetcher.fetchSbom(request);
    }

    @Override
    public void publishToDependencyTrack(String projectName, String projectVersion, byte[] sbomJsonBytes) {
        bomPublisher.publish(projectName, projectVersion, sbomJsonBytes);
    }
}

