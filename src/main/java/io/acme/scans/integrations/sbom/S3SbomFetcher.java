package io.acme.scans.integrations.sbom;

import io.acme.scans.config.SbomS3Config;
import io.acme.scans.domain.ScanRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

@ApplicationScoped
public class S3SbomFetcher implements SbomFetcher {
    private static final Logger LOG = Logger.getLogger(S3SbomFetcher.class);

    private final SbomS3Config config;
    private final S3Client s3;

    public S3SbomFetcher(SbomS3Config config) {
        this.config = config;

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(config.region()))
                .serviceConfiguration(S3Configuration.builder().build())
                .credentialsProvider(credentialsProvider(config));

        String endpointOverride = config.endpointOverride();
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder = builder.endpointOverride(URI.create(endpointOverride.trim()));
        }

        this.s3 = builder.build();
    }

    private static AwsCredentialsProvider credentialsProvider(SbomS3Config config) {
        SbomS3Config.Credentials creds = config.credentials();
        if (creds == null) {
            return DefaultCredentialsProvider.create();
        }

        String accessKeyId = creds.accessKeyId();
        String secretAccessKey = creds.secretAccessKey();
        if (accessKeyId == null || accessKeyId.isBlank() || secretAccessKey == null || secretAccessKey.isBlank()) {
            return DefaultCredentialsProvider.create();
        }

        String sessionToken = creds.sessionToken()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse("");
        if (!sessionToken.isEmpty()) {
            return StaticCredentialsProvider.create(AwsSessionCredentials.create(
                    accessKeyId.trim(),
                    secretAccessKey.trim(),
                    sessionToken
            ));
        }

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(
                accessKeyId.trim(),
                secretAccessKey.trim()
        ));
    }

    @Override
    public byte[] fetchSbom(ScanRequest request) {
        String key = "%d/%s/%s.json".formatted(request.appId(), request.componentName(), request.buildId());
        try {
            LOG.infov("Fetching SBOM from S3. bucket={0}, key={1}", config.bucket(), key);
            ResponseBytes<?> bytes = s3.getObject(
                    GetObjectRequest.builder()
                            .bucket(config.bucket())
                            .key(key)
                            .build(),
                    ResponseTransformer.toBytes()
            );
            return bytes.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new IllegalStateException("SBOM not found in S3. bucket=" + config.bucket() + " key=" + key, e);
        } catch (S3Exception e) {
            throw new IllegalStateException("Failed to fetch SBOM from S3. bucket=" + config.bucket() + " key=" + key, e);
        } catch (SdkException e) {
            throw new IllegalStateException("Failed to fetch SBOM from S3. bucket=" + config.bucket() + " key=" + key, e);
        }
    }
}

