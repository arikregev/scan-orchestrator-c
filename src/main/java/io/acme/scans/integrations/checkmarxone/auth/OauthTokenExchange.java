package io.acme.scans.integrations.checkmarxone.auth;

import java.net.URI;

/**
 * Wraps IAM token HTTP calls so auth providers can be unit-tested without a live server.
 */
public interface OauthTokenExchange {

    OauthTokenResponse clientCredentials(URI tokenEndpoint, String clientId, String clientSecret, String scope);

    /**
     * @param clientSecret {@code null} or blank to omit {@code client_secret} from the form body
     * @param scope        {@code null} or blank to omit {@code scope} from the form body
     */
    OauthTokenResponse refresh(URI tokenEndpoint, String clientId, String refreshToken, String clientSecret, String scope);
}
