package io.acme.scans.integrations.checkmarxone.auth;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "checkmarx-one-oauth")
public interface OauthTokenClient {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    OauthTokenResponse tokenClientCredentials(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("scope") String scope
    );

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    OauthTokenResponse tokenRefresh(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("refresh_token") String refreshToken
    );

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    OauthTokenResponse tokenRefreshWithScope(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("refresh_token") String refreshToken,
            @FormParam("scope") String scope
    );

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    OauthTokenResponse tokenRefreshWithSecret(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("refresh_token") String refreshToken,
            @FormParam("client_secret") String clientSecret
    );

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    OauthTokenResponse tokenRefreshWithSecretAndScope(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("refresh_token") String refreshToken,
            @FormParam("client_secret") String clientSecret,
            @FormParam("scope") String scope
    );
}
