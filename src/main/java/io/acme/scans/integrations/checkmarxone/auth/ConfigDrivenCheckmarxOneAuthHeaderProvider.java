package io.acme.scans.integrations.checkmarxone.auth;

import io.acme.scans.config.CheckmarxOneConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ConfigDrivenCheckmarxOneAuthHeaderProvider implements CheckmarxOneAuthHeaderProvider {
    private final CheckmarxOneConfig config;

    // cached for oauth2 mode
    private volatile String cachedHeaderValue;
    private volatile long cachedUntilEpochMs;

    public ConfigDrivenCheckmarxOneAuthHeaderProvider(CheckmarxOneConfig config) {
        this.config = config;
    }

    @Override
    public String authorizationHeaderValue() {
        String mode = (config.auth().mode() == null ? "bearer" : config.auth().mode()).trim().toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "bearer" -> bearer();
            case "oauth2" -> oauth2();
            default -> throw new IllegalArgumentException("Unsupported checkmarx-one.auth.mode: " + mode);
        };
    }

    private String bearer() {
        String token = config.auth().bearerToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("checkmarx-one.auth.bearer-token is required for bearer auth mode");
        }
        return "Bearer " + token.trim();
    }

    private String oauth2() {
        long now = System.currentTimeMillis();
        String cached = cachedHeaderValue;
        if (cached != null && now < cachedUntilEpochMs) return cached;

        synchronized (this) {
            now = System.currentTimeMillis();
            cached = cachedHeaderValue;
            if (cached != null && now < cachedUntilEpochMs) return cached;

            String tokenUrl = config.auth().tokenUrl();
            if (tokenUrl == null || tokenUrl.isBlank()) {
                throw new IllegalStateException("checkmarx-one.auth.token-url is required for oauth2 auth mode");
            }
            String clientId = config.auth().clientId();
            String clientSecret = config.auth().clientSecret();
            if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
                throw new IllegalStateException("checkmarx-one.auth.client-id and checkmarx-one.auth.client-secret are required for oauth2 auth mode");
            }

            OauthTokenClient tokenClient = RestClientBuilder.newBuilder()
                    .baseUri(URI.create(tokenUrl))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build(OauthTokenClient.class);

            String scope = config.auth().scope();
            OauthTokenResponse resp = tokenClient.token("client_credentials", clientId.trim(), clientSecret.trim(), scope == null ? "" : scope.trim());
            if (resp == null || resp.accessToken() == null || resp.accessToken().isBlank()) {
                throw new IllegalStateException("OAuth2 token endpoint did not return access_token");
            }
            String tokenType = (resp.tokenType() == null || resp.tokenType().isBlank()) ? "Bearer" : resp.tokenType().trim();
            String header = tokenType + " " + resp.accessToken().trim();

            // cache with a safety buffer
            long expiresInSec = resp.expiresIn() == null ? 60 : Math.max(1, resp.expiresIn());
            cachedHeaderValue = header;
            cachedUntilEpochMs = System.currentTimeMillis() + Math.max(1_000, (expiresInSec - 10) * 1000);

            return header;
        }
    }
}

