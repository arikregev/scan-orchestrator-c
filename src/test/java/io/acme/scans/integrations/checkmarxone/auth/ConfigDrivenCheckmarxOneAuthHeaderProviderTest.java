package io.acme.scans.integrations.checkmarxone.auth;

import io.acme.scans.config.CheckmarxOneConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigDrivenCheckmarxOneAuthHeaderProviderTest {

    @Mock
    CheckmarxOneConfig config;

    @Mock
    CheckmarxOneConfig.Auth auth;

    @Mock
    OauthTokenExchange exchange;

    ConfigDrivenCheckmarxOneAuthHeaderProvider provider;

    @BeforeEach
    void setUp() {
        when(config.auth()).thenReturn(auth);
        provider = new ConfigDrivenCheckmarxOneAuthHeaderProvider(config, exchange);
    }

    @Test
    void refreshTokenModeUsesComposedIamUrlAndCachesHeader() {
        when(auth.mode()).thenReturn("refresh_token");
        when(auth.tokenUrl()).thenReturn(Optional.empty());
        when(auth.tenant()).thenReturn(Optional.of("acme"));
        when(auth.iamBaseUrl()).thenReturn(Optional.of("https://iam.checkmarx.net/"));
        when(auth.clientId()).thenReturn(Optional.of("ast-app"));
        when(auth.clientSecret()).thenReturn(Optional.empty());
        when(auth.scope()).thenReturn(Optional.empty());
        when(auth.refreshToken()).thenReturn(Optional.of("rt-initial"));

        when(exchange.refresh(
                eq(URI.create("https://iam.checkmarx.net/auth/realms/acme/protocol/openid-connect/token")),
                eq("ast-app"),
                eq("rt-initial"),
                isNull(),
                isNull()))
                .thenReturn(new OauthTokenResponse("access-1", "Bearer", 3600L, null));

        assertEquals("Bearer access-1", provider.authorizationHeaderValue());
        assertEquals("Bearer access-1", provider.authorizationHeaderValue());

        verify(exchange, times(1)).refresh(any(), any(), any(), any(), any());
    }

    @Test
    void refreshTokenModeRotatesInMemoryRefreshTokenAfterRotationResponse() throws Exception {
        when(auth.mode()).thenReturn("refresh_token");
        when(auth.tokenUrl()).thenReturn(Optional.empty());
        when(auth.tenant()).thenReturn(Optional.of("acme"));
        when(auth.iamBaseUrl()).thenReturn(Optional.of("https://iam.checkmarx.net"));
        when(auth.clientId()).thenReturn(Optional.of("ast-app"));
        when(auth.clientSecret()).thenReturn(Optional.empty());
        when(auth.scope()).thenReturn(Optional.empty());
        when(auth.refreshToken()).thenReturn(Optional.of("rt-initial"));

        URI endpoint = URI.create("https://iam.checkmarx.net/auth/realms/acme/protocol/openid-connect/token");

        when(exchange.refresh(eq(endpoint), eq("ast-app"), eq("rt-initial"), isNull(), isNull()))
                .thenReturn(new OauthTokenResponse("access-1", "Bearer", 12L, "rt-rotated"));
        when(exchange.refresh(eq(endpoint), eq("ast-app"), eq("rt-rotated"), isNull(), isNull()))
                .thenReturn(new OauthTokenResponse("access-2", "Bearer", 3600L, null));

        assertEquals("Bearer access-1", provider.authorizationHeaderValue());

        Thread.sleep(2_500L);

        assertEquals("Bearer access-2", provider.authorizationHeaderValue());

        verify(exchange, times(1)).refresh(eq(endpoint), eq("ast-app"), eq("rt-initial"), isNull(), isNull());
        verify(exchange, times(1)).refresh(eq(endpoint), eq("ast-app"), eq("rt-rotated"), isNull(), isNull());
    }

    @Test
    void oauth2UsesComposedUrlWhenTokenUrlBlank() {
        when(auth.mode()).thenReturn("oauth2");
        when(auth.tokenUrl()).thenReturn(Optional.empty());
        when(auth.tenant()).thenReturn(Optional.of("realm-x"));
        when(auth.iamBaseUrl()).thenReturn(Optional.of("https://iam.example.com"));
        when(auth.clientId()).thenReturn(Optional.of("cid"));
        when(auth.clientSecret()).thenReturn(Optional.of("sec"));
        when(auth.scope()).thenReturn(Optional.empty());

        when(exchange.clientCredentials(
                eq(URI.create("https://iam.example.com/auth/realms/realm-x/protocol/openid-connect/token")),
                eq("cid"),
                eq("sec"),
                eq("")))
                .thenReturn(new OauthTokenResponse("oa", "Bearer", 3600L, null));

        assertEquals("Bearer oa", provider.authorizationHeaderValue());

        verify(exchange, times(1)).clientCredentials(any(), any(), any(), any());
    }

    @Test
    void refreshPassesOptionalSecretAndScopeWhenSet() {
        when(auth.mode()).thenReturn("refresh_token");
        when(auth.tokenUrl()).thenReturn(Optional.of("https://custom/token"));
        when(auth.clientId()).thenReturn(Optional.of("ast-app"));
        when(auth.clientSecret()).thenReturn(Optional.of("shh"));
        when(auth.scope()).thenReturn(Optional.of("openid"));
        when(auth.refreshToken()).thenReturn(Optional.of("rt"));

        when(exchange.refresh(
                eq(URI.create("https://custom/token")),
                eq("ast-app"),
                eq("rt"),
                eq("shh"),
                eq("openid")))
                .thenReturn(new OauthTokenResponse("acc", "Bearer", 60L, null));

        assertEquals("Bearer acc", provider.authorizationHeaderValue());

        verify(exchange).refresh(
                eq(URI.create("https://custom/token")),
                eq("ast-app"),
                eq("rt"),
                eq("shh"),
                eq("openid"));
    }
}
