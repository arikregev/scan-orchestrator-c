package io.acme.scans.integrations.checkmarxone.auth;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "checkmarx-one-oauth")
public interface OauthTokenClient {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    OauthTokenResponse token(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("scope") String scope
    );
}

