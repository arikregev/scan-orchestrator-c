package io.acme.scans.integrations.checkmarxone.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OauthTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn
) {
}

