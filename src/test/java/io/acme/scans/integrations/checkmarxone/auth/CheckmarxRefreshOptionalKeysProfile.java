package io.acme.scans.integrations.checkmarxone.auth;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Boots the app with {@code refresh_token} auth and blank optional Checkmarx keys, without starting
 * the Temporal worker or Kafka Dev Services.
 */
public class CheckmarxRefreshOptionalKeysProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.ofEntries(
                Map.entry("quarkus.arc.exclude-types", "io.acme.scans.temporal.worker.TemporalWorkerRunner"),
                Map.entry("quarkus.kafka.devservices.enabled", "false"),
                Map.entry("mp.messaging.incoming.ocsf-scan-activity.enabled", "false"),
                Map.entry("checkmarx-one.base-url", "https://ast.checkmarx.com"),
                Map.entry("checkmarx-one.auth.mode", "refresh_token"),
                Map.entry("checkmarx-one.auth.iam-base-url", "https://iam.checkmarx.net"),
                Map.entry("checkmarx-one.auth.tenant", "test-tenant"),
                Map.entry("checkmarx-one.auth.client-id", "ast-app"),
                Map.entry("checkmarx-one.auth.refresh-token", "dummy-refresh"));
    }
}
