package io.acme.scans.integrations.checkmarxone.auth;

import io.acme.scans.config.CheckmarxOneConfig;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(CheckmarxRefreshOptionalKeysProfile.class)
class CheckmarxOneRefreshTokenConfigStartupTest {

    @Inject
    CheckmarxOneConfig checkmarxOneConfig;

    @Test
    void contextStartsWithRefreshModeAndBlankOptionalSecrets() {
        assertEquals("refresh_token", checkmarxOneConfig.auth().mode());
        assertTrue(checkmarxOneConfig.auth().clientSecret().isEmpty());
        assertTrue(checkmarxOneConfig.auth().scope().isEmpty());
        assertTrue(checkmarxOneConfig.auth().bearerToken().isEmpty());
        assertTrue(checkmarxOneConfig.auth().tokenUrl().isEmpty());
        assertEquals(Optional.of("test-tenant"), checkmarxOneConfig.auth().tenant());
        assertEquals(Optional.of("https://iam.checkmarx.net"), checkmarxOneConfig.auth().iamBaseUrl());
    }
}
