package io.acme.scans.integrations.checkmarxsca;

public record ScaScanRequest(ScaScanProject project) {
    public record ScaScanProject(String id, String type, ScaScanHandler handler) {
    }

    public record ScaScanHandler(String url) {
    }
}

