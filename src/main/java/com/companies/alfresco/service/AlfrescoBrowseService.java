package com.companies.alfresco.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AlfrescoBrowseService {

    private final RestTemplate restTemplate;
    private final AlfrescoAuthService auth;
    private final String alfrescoBaseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public AlfrescoBrowseService(RestTemplate restTemplate,
                                 AlfrescoAuthService auth,
                                 @Value("${alfresco.base-url}") String alfrescoBaseUrl) {
        this.restTemplate = restTemplate;
        this.auth = auth;
        this.alfrescoBaseUrl = alfrescoBaseUrl;
    }

    public String listChildrenByRelativePath(String relativePath) {

         // encode only spaces, keep slashes
        String encoded = relativePath.replace(" ", "%20");
        // String encoded = URLEncoder.encode(relativePath, StandardCharsets.UTF_8);
        String url = alfrescoBaseUrl
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-/children?relativePath="
                + encoded;

        HttpHeaders headers = auth.authHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        return response.getBody();
    }

    public String listChildrenByFolderId(String folderId) {
        String url = alfrescoBaseUrl
            + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
            + folderId + "/children?maxItems=1000";

        HttpHeaders headers = auth.authHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        return response.getBody();
}

// To get file entries (ids + names + mimetype)
public JsonNode listChildrenJson(String folderId) {
        String url = alfrescoBaseUrl
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + folderId + "/children?maxItems=1000";

        HttpHeaders headers = auth.authHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        try {
            return mapper.readTree(resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse list response", e);
        }
    }

}
