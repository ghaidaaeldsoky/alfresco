package com.companies.alfresco.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class AlfrescoUploadService {

    private final RestTemplate restTemplate;

    @Value("${alfresco.base-url}")
    private String alfrescoBaseUrl;

    @Value("${alfresco.username}")
    private String alfrescoUsername;

    @Value("${alfresco.password}")
    private String alfrescoPassword;

    public AlfrescoUploadService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String uploadBase64File(String fileName,
                                   String contentBase64,
                                   long size,
                                   String parentFolderId,
                                   String mimeType) {

        // 1) Decode Base64
        byte[] fileBytes = Base64.getDecoder().decode(contentBase64);

        if (fileBytes.length != size) {
            throw new RuntimeException("Size mismatch: expected " + size + " but got " + fileBytes.length);
        }

        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }

        if (parentFolderId == null || parentFolderId.isEmpty()) {
            parentFolderId = "-root-"; // or some default folder ID
        }

        // 2) Build Alfresco URL
        String url = alfrescoBaseUrl
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + parentFolderId
                + "/children";

        // 3) Build multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        body.add("filedata", fileResource);
        body.add("name", fileName);
        body.add("nodeType", "cm:content");

        // 4) Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        // Basic Auth
        String auth = alfrescoUsername + ":" + alfrescoPassword;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        // 5) Call Alfresco
        ResponseEntity<String> response =
                restTemplate.postForEntity(url, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Alfresco upload failed: HTTP "
                    + response.getStatusCode() + " body: " + response.getBody());
        }

        return response.getBody(); // Alfresco JSON (contains entry.id, etc.)
    }
}
