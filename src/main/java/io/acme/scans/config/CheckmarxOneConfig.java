package io.acme.scans.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "checkmarx-one")
public interface CheckmarxOneConfig {
    @WithName("base-url")
    String baseUrl();

    Auth auth();

    interface Auth {
        /**
         * Supported values: bearer, oauth2
         */
        @WithDefault("bearer")
        String mode();

        @WithName("bearer-token")
        String bearerToken();

        @WithName("token-url")
        String tokenUrl();

        @WithName("client-id")
        String clientId();

        @WithName("client-secret")
        String clientSecret();

        @WithDefault("")
        String scope();
    }
}

