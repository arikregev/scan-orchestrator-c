package io.acme.scans.integrations.dependencytrack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFormDataTest {
    @Test
    void includesRequiredFieldsAndBoundary() {
        MultipartFormData mp = new MultipartFormData()
                .addField("autoCreate", "true")
                .addField("projectName", "1:comp")
                .addField("projectVersion", "build")
                .addFile("bom", "sbom.json", "application/json", "{\"bom\":true}".getBytes());

        byte[] body = mp.build();
        String s = new String(body);

        assertTrue(s.contains("name=\"autoCreate\""));
        assertTrue(s.contains("\r\n\r\ntrue\r\n"));
        assertTrue(s.contains("name=\"projectName\""));
        assertTrue(s.contains("\r\n\r\n1:comp\r\n"));
        assertTrue(s.contains("name=\"projectVersion\""));
        assertTrue(s.contains("\r\n\r\nbuild\r\n"));
        assertTrue(s.contains("name=\"bom\"; filename=\"sbom.json\""));
        assertTrue(s.contains("Content-Type: application/json"));
        assertTrue(s.contains("{\"bom\":true}"));
        assertTrue(s.contains("--" + mp.boundary() + "--"));
    }
}

