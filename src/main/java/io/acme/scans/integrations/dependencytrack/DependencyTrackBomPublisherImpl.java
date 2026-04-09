package io.acme.scans.integrations.dependencytrack;

import io.acme.scans.config.DependencyTrackConfig;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class DependencyTrackBomPublisherImpl implements DependencyTrackBomPublisher {
    private final DependencyTrackConfig config;
    private final HttpClient httpClient;

    public DependencyTrackBomPublisherImpl(DependencyTrackConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public void publish(String projectName, String projectVersion, byte[] sbomJsonBytes) {
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new IllegalStateException("dependency-track.api-key is required");
        }
        if (config.baseUrl() == null || config.baseUrl().isBlank()) {
            throw new IllegalStateException("dependency-track.base-url is required");
        }

        MultipartFormData mp = new MultipartFormData()
                .addField("autoCreate", "true")
                .addField("projectName", projectName)
                .addField("projectVersion", projectVersion)
                .addFile("bom", "sbom.json", "application/json", sbomJsonBytes);

        byte[] body = mp.build();
        String url = stripTrailingSlash(config.baseUrl()) + "/api/v1/bom";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("X-Api-Key", config.apiKey().trim())
                .header("Content-Type", "multipart/form-data; boundary=" + mp.boundary())
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        try {
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Dependency-Track BOM upload failed. status=" + code + " body=" + truncate(resp.body()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Dependency-Track BOM upload failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Dependency-Track BOM upload interrupted", e);
        }
    }

    private static String stripTrailingSlash(String s) {
        String t = s.trim();
        while (t.endsWith("/")) t = t.substring(0, t.length() - 1);
        return t;
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}

