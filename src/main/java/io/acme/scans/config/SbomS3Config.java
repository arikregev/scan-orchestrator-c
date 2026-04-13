package io.acme.scans.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "sbom.s3")
public interface SbomS3Config {
    String bucket();

    /**
     * AWS region, e.g. us-east-1.
     */
    String region();

    /**
     * Optional static credentials (useful for dedicated IAM user, MinIO, localstack).
     * If omitted/blank, the default AWS credential provider chain is used.
     */
    Credentials credentials();

    /**
     * Optional S3 endpoint override (useful for MinIO/localstack), e.g. http://localhost:9000
     */
    @WithName("endpoint-override")
    String endpointOverride();

    interface Credentials {
        @WithName("access-key-id")
        String accessKeyId();

        @WithName("secret-access-key")
        String secretAccessKey();

        @WithName("session-token")
        Optional<String> sessionToken();
    }
}

