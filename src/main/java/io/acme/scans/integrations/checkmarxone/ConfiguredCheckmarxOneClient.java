package io.acme.scans.integrations.checkmarxone;

import io.acme.scans.config.CheckmarxOneConfig;
import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.integrations.checkmarxone.auth.CheckmarxOneAuthHeaderProvider;
import io.acme.scans.integrations.checkmarxsca.ScaCreateProjectRequest;
import io.acme.scans.integrations.checkmarxsca.ScaProjectsApi;
import io.acme.scans.integrations.checkmarxsca.ScaScanRequest;
import io.acme.scans.integrations.checkmarxsca.ScaScansApi;
import io.acme.scans.integrations.checkmarxsca.ScaUploadsApi;
import io.acme.scans.integrations.http.HttpPutUploader;
import io.acme.scans.config.CheckmarxScaConfig;
import io.acme.scans.integrations.sbom.SbomFetcher;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@ApplicationScoped
public class ConfiguredCheckmarxOneClient implements CheckmarxOneClient {
    private static final Logger LOG = Logger.getLogger(ConfiguredCheckmarxOneClient.class);

    private final CheckmarxOneConfig config;
    private final CheckmarxOneAuthHeaderProvider authHeaderProvider;
    private final CheckmarxScaConfig scaConfig;
    private final ScaUploadsApi scaUploadsApi;
    private final ScaProjectsApi scaProjectsApi;
    private final ScaScansApi scaScansApi;
    private final HttpPutUploader uploader;
    private final SbomFetcher sbomFetcher;

    public ConfiguredCheckmarxOneClient(CheckmarxOneConfig config,
                                        CheckmarxOneAuthHeaderProvider authHeaderProvider,
                                        CheckmarxScaConfig scaConfig,
                                        @RestClient ScaUploadsApi scaUploadsApi,
                                        @RestClient ScaProjectsApi scaProjectsApi,
                                        @RestClient ScaScansApi scaScansApi,
                                        HttpPutUploader uploader,
                                        SbomFetcher sbomFetcher) {
        this.config = config;
        this.authHeaderProvider = authHeaderProvider;
        this.scaConfig = scaConfig;
        this.scaUploadsApi = scaUploadsApi;
        this.scaProjectsApi = scaProjectsApi;
        this.scaScansApi = scaScansApi;
        this.uploader = uploader;
        this.sbomFetcher = sbomFetcher;
    }

    @Override
    public String submitScan(ScanTool tool, ScanRequest request) {
        String auth = authHeaderProvider.authorizationHeaderValue();

        if (tool == ScanTool.SCA_MANIFEST) {
            // Use documented Checkmarx SCA REST API: create project under team(appId) -> upload -> scan
            String team = scaConfig.teamPrefix() + request.appId();
            String projectName = request.componentName();

            var project = scaProjectsApi.create(new ScaCreateProjectRequest(projectName, List.of(team)));

            var upload = scaUploadsApi.createUpload();

            byte[] sbomJson = sbomFetcher.fetchSbom(request);
            byte[] zipBytes = zipSingleJson("%s.json".formatted(request.buildId()), sbomJson);

            uploader.putZip(upload.url(), auth, zipBytes);

            var scanResp = scaScansApi.create(new ScaScanRequest(
                    new ScaScanRequest.ScaScanProject(
                            project.id(),
                            "upload",
                            new ScaScanRequest.ScaScanHandler(upload.url())
                    )
            ));

            return scanResp.id();
        }

        // SAST + others: still stubbed until we wire Checkmarx One /api/scans payload from Stoplight schema.
        LOG.debugf("Checkmarx One submitScan stub (pending /api/scans wiring). baseUrl=%s, tool=%s, authHeaderPrefix=%s",
                config.baseUrl(), tool, auth.split(" ")[0]);
        return tool.name().toLowerCase() + "-" + UUID.randomUUID();
    }

    @Override
    public String getScanStatus(ScanTool tool, String scanId) {
        // Phase 1: stub status.
        authHeaderProvider.authorizationHeaderValue();
        return "SUBMITTED";
    }

    private static byte[] zipSingleJson(String filename, byte[] jsonBytes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                ZipEntry entry = new ZipEntry(filename);
                zos.putNextEntry(entry);
                zos.write(jsonBytes);
                zos.closeEntry();
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build zip payload", e);
        }
    }
}

