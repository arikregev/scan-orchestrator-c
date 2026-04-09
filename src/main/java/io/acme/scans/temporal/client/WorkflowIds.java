package io.acme.scans.temporal.client;

import io.acme.scans.domain.ScanRequest;

import java.nio.charset.StandardCharsets;

public final class WorkflowIds {
    private WorkflowIds() {
    }

    public static String forRequest(ScanRequest request) {
        // Keep this stable and deterministic for idempotency.
        // Avoid unsafe chars in componentName/buildId by percent-encoding.
        return "scan:%s:%s:%s:%s".formatted(
                request.tool().name(),
                request.appId(),
                pct(request.componentName()),
                pct(request.buildId())
        );
    }

    private static String pct(String s) {
        if (s == null) return "null";
        // Minimal percent-encoding for common delimiters/spaces.
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        StringBuilder out = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            int c = b & 0xff;
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-') {
                out.append((char) c);
            } else {
                out.append('%');
                out.append(Character.toUpperCase(Character.forDigit((c >> 4) & 0xF, 16)));
                out.append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
            }
        }
        return out.toString();
    }
}

