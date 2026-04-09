package io.acme.scans.integrations.http;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class HttpPutUploader {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public void putZip(String url, String authorizationHeaderValue, byte[] zipBytes) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(5))
                    .header("Content-Type", "application/zip")
                    .header("Authorization", authorizationHeaderValue)
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(zipBytes))
                    .build();
            HttpResponse<Void> resp = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            int code = resp.statusCode();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Upload PUT failed. status=" + code);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Upload PUT failed", e);
        }
    }
}

