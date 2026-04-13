package io.acme.scans.integrations.checkmarxone.auth;

import io.acme.scans.config.CheckmarxOneConfig;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class ConfigDrivenCheckmarxOneAuthHeaderProvider implements CheckmarxOneAuthHeaderProvider {
    private final CheckmarxOneConfig config;
    private final OauthTokenExchange oauthTokenExchange;

    private volatile String cachedHeaderValue;
    private volatile long cachedUntilEpochMs;

    /** In-memory refresh token; when null, {@link CheckmarxOneConfig.Auth#refreshToken()} is used. */
    private volatile String rotatedRefreshToken;

    public ConfigDrivenCheckmarxOneAuthHeaderProvider(CheckmarxOneConfig config, OauthTokenExchange oauthTokenExchange) {
        this.config = config;
        this.oauthTokenExchange = oauthTokenExchange;
    }

    @Override
    public String authorizationHeaderValue() {
        String mode = (config.auth().mode() == null ? "bearer" : config.auth().mode()).trim().toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "bearer" -> bearer();
            case "oauth2" -> oauth2();
            case "refresh_token" -> refreshTokenGrant();
            default -> throw new IllegalArgumentException("Unsupported checkmarx-one.auth.mode: " + mode);
        };
    }

    private String bearer() {
        String token = opt(config.auth().bearerToken());
        if (token.isEmpty()) {
            throw new IllegalStateException("checkmarx-one.auth.bearer-token is required for bearer auth mode");
        }
        return "Bearer " + token;
    }

    private String resolveTokenEndpointUrl() {
        String override = opt(config.auth().tokenUrl());
        if (!override.isEmpty()) {
            return override;
        }
        String tenant = opt(config.auth().tenant());
        if (tenant.isEmpty()) {
            throw new IllegalStateException(
                    "checkmarx-one.auth.tenant is required when checkmarx-one.auth.token-url is blank");
        }
        String iam = opt(config.auth().iamBaseUrl());
        if (iam.isEmpty()) {
            iam = "https://iam.checkmarx.net";
        }
        return stripTrailingSlash(iam) + "/auth/realms/" + tenant + "/protocol/openid-connect/token";
    }

    private String oauth2() {
        long now = System.currentTimeMillis();
        String cached = cachedHeaderValue;
        if (cached != null && now < cachedUntilEpochMs) {
            return cached;
        }

        synchronized (this) {
            now = System.currentTimeMillis();
            cached = cachedHeaderValue;
            if (cached != null && now < cachedUntilEpochMs) {
                return cached;
            }

            String tokenUrl = resolveTokenEndpointUrl();
            String clientId = opt(config.auth().clientId());
            String clientSecret = opt(config.auth().clientSecret());
            if (clientId.isEmpty() || clientSecret.isEmpty()) {
                throw new IllegalStateException(
                        "checkmarx-one.auth.client-id and checkmarx-one.auth.client-secret are required for oauth2 auth mode");
            }

            String scope = opt(config.auth().scope());
            OauthTokenResponse resp = oauthTokenExchange.clientCredentials(
                    URI.create(tokenUrl),
                    clientId,
                    clientSecret,
                    scope);

            return cacheAndReturnHeader(resp);
        }
    }

    private String refreshTokenGrant() {
        long now = System.currentTimeMillis();
        String cached = cachedHeaderValue;
        if (cached != null && now < cachedUntilEpochMs) {
            return cached;
        }

        synchronized (this) {
            now = System.currentTimeMillis();
            cached = cachedHeaderValue;
            if (cached != null && now < cachedUntilEpochMs) {
                return cached;
            }

            String tokenUrl = resolveTokenEndpointUrl();
            String clientId = opt(config.auth().clientId());
            if (clientId.isEmpty()) {
                throw new IllegalStateException("checkmarx-one.auth.client-id is required for refresh_token auth mode");
            }

            String refresh = rotatedRefreshToken != null && !rotatedRefreshToken.isBlank()
                    ? rotatedRefreshToken.trim()
                    : opt(config.auth().refreshToken());
            if (refresh.isEmpty()) {
                throw new IllegalStateException("checkmarx-one.auth.refresh-token is required for refresh_token auth mode");
            }

            String clientSecret = opt(config.auth().clientSecret());
            String scope = opt(config.auth().scope());
            OauthTokenResponse resp = oauthTokenExchange.refresh(
                    URI.create(tokenUrl),
                    clientId,
                    refresh,
                    clientSecret.isEmpty() ? null : clientSecret,
                    scope.isEmpty() ? null : scope);

            if (resp.refreshToken() != null && !resp.refreshToken().isBlank()) {
                rotatedRefreshToken = resp.refreshToken().trim();
            }

            return cacheAndReturnHeader(resp);
        }
    }

    private String cacheAndReturnHeader(OauthTokenResponse resp) {
        if (resp == null || resp.accessToken() == null || resp.accessToken().isBlank()) {
            throw new IllegalStateException("OAuth2 token endpoint did not return access_token");
        }
        String tokenType = (resp.tokenType() == null || resp.tokenType().isBlank()) ? "Bearer" : resp.tokenType().trim();
        String header = tokenType + " " + resp.accessToken().trim();

        long expiresInSec = resp.expiresIn() == null ? 60 : Math.max(1, resp.expiresIn());
        cachedHeaderValue = header;
        cachedUntilEpochMs = System.currentTimeMillis() + Math.max(1_000, (expiresInSec - 10) * 1000);

        return header;
    }

    private static String opt(Optional<String> o) {
        return o.map(String::trim).filter(s -> !s.isEmpty()).orElse("");
    }

    private static String stripTrailingSlash(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '/') {
            end--;
        }
        return s.substring(0, end);
    }
}
