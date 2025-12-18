package com.companies.alfresco.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AlfrescoDocumentService {

    private final RestTemplate restTemplate;
    private final AlfrescoAuthService auth;
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public AlfrescoDocumentService(RestTemplate restTemplate,
                                   AlfrescoAuthService auth,
                                   @Value("${alfresco.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.auth = auth;
        this.baseUrl = baseUrl;
    }

    public UploadResult upload(String folderId,
                               String fileName,
                               byte[] bytes,
                               String mimeType,
                               String serviceId) {

        String url = baseUrl + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + folderId + "/children";

        HttpHeaders headers = auth.authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource file = new ByteArrayResource(bytes) {
            @Override public String getFilename() { return fileName; }
        };

        body.add("filedata", file);
        body.add("name", fileName);
        body.add("nodeType", "cm:content");

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        String json = response.getBody();
        String nodeId = extractNodeId(json);

        if (nodeId != null) {
            updateDescription(nodeId, "serviceId=" + serviceId);
        }

        return new UploadResult(nodeId, json);
    }

    public String extractNodeId(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path("entry").path("id").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateDescription(String nodeId, String description) {
        String url = baseUrl + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + nodeId;

        HttpHeaders headers = auth.authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        String body = "{ \"properties\": { \"cm:description\": \"" + escape(description) + "\" } }";

        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }

    public static class UploadResult {
        public final String nodeId;
        public final String rawJson;
        public UploadResult(String nodeId, String rawJson) {
            this.nodeId = nodeId;
            this.rawJson = rawJson;
        }
    }
}
