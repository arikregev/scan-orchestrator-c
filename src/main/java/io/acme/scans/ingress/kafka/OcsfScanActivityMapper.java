package io.acme.scans.ingress.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class OcsfScanActivityMapper {
    private final ObjectMapper objectMapper;

    public OcsfScanActivityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ScanRequest fromJson(String json) {
        final JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }

        Integer appId = optTagValue(root, "app_id")
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .map(Integer::valueOf)
                .orElse(null);

        String componentName = optTagValue(root, "component_name").orElse(null);
        String buildId = optFieldStringValue(root, "build_id").orElse(null);
        String repoUrl = optFieldStringValue(root, "code_repository_url").orElse(null);
        String commitSha = optFieldStringValue(root, "git_commit_id").orElse(null);
        String branchName = optFieldStringValue(root, "branch_name").orElse(null);
        String sourceUrl = optFieldStringValue(root, "source_url").orElse(null);
        String originalEventUid = optText(root, "metadata", "original_event_uid").orElse(null);

        ScanTool tool = optText(root, "scan", "type")
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .map(OcsfScanActivityMapper::toTool)
                .orElse(null);

        return new ScanRequest(appId, componentName, buildId, tool, repoUrl, commitSha, branchName, sourceUrl, originalEventUid);
    }

    private static ScanTool toTool(String scanTypeUpper) {
        return switch (scanTypeUpper) {
            case "SAST" -> ScanTool.SAST;
            case "SCA", "SCA_MANIFEST", "SCA-MANIFEST" -> ScanTool.SCA_MANIFEST;
            case "SCA_BINARY", "SCA_BINARY_UPLOAD", "SCA-BINARY-UPLOAD" -> ScanTool.SCA_BINARY_UPLOAD;
            case "GITLEAKS" -> ScanTool.GITLEAKS;
            default -> throw new IllegalArgumentException("Unsupported scan.type: " + scanTypeUpper);
        };
    }

    private static Optional<String> optTagValue(JsonNode root, String name) {
        JsonNode tags = root.path("tags");
        if (!tags.isArray()) return Optional.empty();
        for (JsonNode tag : tags) {
            String tagName = tag.path("name").asText(null);
            if (name.equals(tagName)) {
                JsonNode value = tag.get("value");
                if (value == null || value.isNull()) return Optional.empty();
                if (value.isTextual()) return Optional.of(value.asText());
                // Some producers wrap value like { "value": "123" }
                return Optional.ofNullable(value.asText(null));
            }
        }
        return Optional.empty();
    }

    private static Optional<String> optFieldStringValue(JsonNode root, String key) {
        // Screenshot shows a protobuf-ish shape: fields[{key:"build_id", value:{string_value:"..."}}]
        JsonNode fields = root.path("fields");
        if (!fields.isArray()) return Optional.empty();
        for (JsonNode field : fields) {
            if (key.equals(field.path("key").asText(null))) {
                JsonNode value = field.path("value");
                if (value.isMissingNode() || value.isNull()) return Optional.empty();
                if (value.isTextual()) return Optional.of(value.asText());
                JsonNode stringValue = value.get("string_value");
                if (stringValue != null && stringValue.isTextual()) return Optional.of(stringValue.asText());
                // fallback
                return Optional.ofNullable(value.asText(null));
            }
        }
        return Optional.empty();
    }

    private static Optional<String> optText(JsonNode root, String... path) {
        JsonNode cur = root;
        for (String p : path) cur = cur.path(p);
        if (cur.isMissingNode() || cur.isNull()) return Optional.empty();
        String v = cur.asText(null);
        return v == null || v.isBlank() ? Optional.empty() : Optional.of(v);
    }
}

