package io.acme.scans.ingress.kafka;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.ingress.ocsf.proto.KeyValueObject;
import io.acme.scans.ingress.ocsf.proto.Metadata;
import io.acme.scans.ingress.ocsf.proto.ScanActivity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class OcsfScanActivityMapper {

    public ScanRequest fromProto(ScanActivity root) {
        if (root == null) {
            throw new IllegalArgumentException("ScanActivity payload is null");
        }

        Integer appId = optTagValue(root.getMetadata(), "app_id")
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .map(Integer::valueOf)
                .orElse(null);

        String componentName = optTagValue(root.getMetadata(), "component_name").orElse(null);
        String buildId = optUnmappedString(root, "build_id").orElse(null);
        String repoUrl = optUnmappedString(root, "code_repository_url").orElse(null);
        String commitSha = optUnmappedString(root, "git_commit_id").orElse(null);
        String branchName = optUnmappedString(root, "branch_name").orElse(null);
        String sourceUrl = optUnmappedString(root, "source_url").orElse(null);

        Optional<String> originalEventUid = Optional.empty();
        if (root.hasMetadata()) {
            String ouid = root.getMetadata().getOriginalEventUid();
            originalEventUid = (ouid == null || ouid.isBlank()) ? Optional.empty() : Optional.of(ouid.trim());
        }

        ScanTool tool = resolveTool(root);

        return new ScanRequest(
                appId,
                componentName,
                buildId,
                tool,
                repoUrl,
                commitSha,
                branchName,
                sourceUrl,
                originalEventUid.orElse(null));
    }

    private static ScanTool resolveTool(ScanActivity root) {
        String scanType = "";
        if (root.hasScan()) {
            String t = root.getScan().getType();
            scanType = t == null ? "" : t.trim();
        }
        if (!scanType.isEmpty()) {
            try {
                return toTool(scanType.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // fall through to product-based resolution
            }
        }
        String product = "";
        if (root.hasMetadata() && root.getMetadata().hasProduct()) {
            String n = root.getMetadata().getProduct().getName();
            product = n == null ? "" : n.trim();
        }
        if ("GITLEAKS".equalsIgnoreCase(product)) {
            return ScanTool.GITLEAKS;
        }
        throw new IllegalArgumentException("Unsupported scan.type: " + scanType + " (product=" + product + ")");
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

    private static Optional<String> optTagValue(Metadata metadata, String name) {
        if (metadata == null) {
            return Optional.empty();
        }
        for (KeyValueObject tag : metadata.getTagsList()) {
            if (name.equals(tag.getName())) {
                return tagValue(tag);
            }
        }
        return Optional.empty();
    }

    /** Prefer {@code value}; if empty, first non-blank entry in {@code values}. */
    private static Optional<String> tagValue(KeyValueObject tag) {
        String v = tag.getValue();
        if (v != null && !v.isBlank()) {
            return Optional.of(v.trim());
        }
        for (String s : tag.getValuesList()) {
            if (s != null && !s.isBlank()) {
                return Optional.of(s.trim());
            }
        }
        return Optional.empty();
    }

    private static Optional<String> optUnmappedString(ScanActivity root, String key) {
        if (!root.hasUnmapped()) {
            return Optional.empty();
        }
        Struct struct = root.getUnmapped();
        if (struct.getFieldsCount() == 0) {
            return Optional.empty();
        }
        Value v = struct.getFieldsMap().get(key);
        if (v == null) {
            return Optional.empty();
        }
        if (v.getKindCase() == Value.KindCase.STRING_VALUE) {
            String s = v.getStringValue();
            if (s == null || s.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(s.trim());
        }
        return Optional.empty();
    }
}
