package io.acme.scans.integrations.checkmarxone.auth;

public interface CheckmarxOneAuthHeaderProvider {
    /**
     * @return value for HTTP Authorization header, e.g. "Bearer eyJ..."
     */
    String authorizationHeaderValue();
}

