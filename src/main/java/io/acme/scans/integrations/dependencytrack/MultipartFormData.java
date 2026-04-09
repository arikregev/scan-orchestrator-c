package io.acme.scans.integrations.dependencytrack;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class MultipartFormData {
    private final String boundary;
    private final List<byte[]> parts = new ArrayList<>();

    MultipartFormData() {
        this.boundary = "----dt-" + UUID.randomUUID();
    }

    String boundary() {
        return boundary;
    }

    MultipartFormData addField(String name, String value) {
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"\r\n" +
                "\r\n";
        parts.add(header.getBytes(StandardCharsets.UTF_8));
        parts.add((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        parts.add("\r\n".getBytes(StandardCharsets.UTF_8));
        return this;
    }

    MultipartFormData addFile(String name, String filename, String contentType, byte[] bytes) {
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "\r\n";
        parts.add(header.getBytes(StandardCharsets.UTF_8));
        parts.add(bytes == null ? new byte[0] : bytes);
        parts.add("\r\n".getBytes(StandardCharsets.UTF_8));
        return this;
    }

    byte[] build() {
        byte[] closing = ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        int size = closing.length;
        for (byte[] p : parts) size += p.length;
        byte[] out = new byte[size];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        System.arraycopy(closing, 0, out, pos, closing.length);
        return out;
    }
}

