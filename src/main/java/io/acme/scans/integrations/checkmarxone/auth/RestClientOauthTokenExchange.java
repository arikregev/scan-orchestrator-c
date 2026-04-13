package io.acme.scans.integrations.checkmarxone.auth;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class RestClientOauthTokenExchange implements OauthTokenExchange {

    @Override
    public OauthTokenResponse clientCredentials(URI tokenEndpoint, String clientId, String clientSecret, String scope) {
        OauthTokenClient client = buildClient(tokenEndpoint);
        String s = scope == null ? "" : scope;
        return client.tokenClientCredentials("client_credentials", clientId, clientSecret, s);
    }

    @Override
    public OauthTokenResponse refresh(URI tokenEndpoint, String clientId, String refreshToken, String clientSecret, String scope) {
        OauthTokenClient client = buildClient(tokenEndpoint);
        boolean hasSecret = clientSecret != null && !clientSecret.isBlank();
        boolean hasScope = scope != null && !scope.isBlank();
        if (hasSecret && hasScope) {
            return client.tokenRefreshWithSecretAndScope("refresh_token", clientId, refreshToken, clientSecret.trim(), scope.trim());
        }
        if (hasSecret) {
            return client.tokenRefreshWithSecret("refresh_token", clientId, refreshToken, clientSecret.trim());
        }
        if (hasScope) {
            return client.tokenRefreshWithScope("refresh_token", clientId, refreshToken, scope.trim());
        }
        return client.tokenRefresh("refresh_token", clientId, refreshToken);
    }

    private static OauthTokenClient buildClient(URI tokenEndpoint) {
        return RestClientBuilder.newBuilder()
                .baseUri(tokenEndpoint)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build(OauthTokenClient.class);
    }
}
