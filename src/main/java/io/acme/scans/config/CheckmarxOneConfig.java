package io.acme.scans.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "checkmarx-one")
public interface CheckmarxOneConfig {
    @WithName("base-url")
    String baseUrl();

    Auth auth();

    interface Auth {
        /**
         * Supported values: bearer, oauth2, refresh_token
         */
        @WithDefault("bearer")
        String mode();

        @WithName("bearer-token")
        Optional<String> bearerToken();

        @WithName("token-url")
        Optional<String> tokenUrl();

        /**
         * IAM host (no trailing path). Used with {@link #tenant()} to compose the token URL when
         * {@link #tokenUrl()} is empty.
         */
        @WithName("iam-base-url")
        Optional<String> iamBaseUrl();

        /**
         * Realm / tenant name for composed IAM token URL.
         */
        Optional<String> tenant();

        @WithName("client-id")
        Optional<String> clientId();

        @WithName("client-secret")
        Optional<String> clientSecret();

        Optional<String> scope();

        @WithName("refresh-token")
        Optional<String> refreshToken();
    }
}
