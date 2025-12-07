package com.companies.alfresco.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class UserFolderService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${alfresco.base-url}")
    private String alfrescoBaseUrl;

    @Value("${alfresco.username}")
    private String alfrescoUsername;

    @Value("${alfresco.password}")
    private String alfrescoPassword;

    @Value("${alfresco.users-root-id}")
    private String usersRootId;

    public UserFolderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String ensureUserFolder(String username) {

        if (username == null || username.isEmpty()) {
            throw new RuntimeException("username is required to create/find user folder");
        }

        // 1) Try to find existing folder
        String existingId = findUserFolder(username);
        if (existingId != null) {
            return existingId;
        }

        // 2) Create folder if not found
        return createUserFolder(username);
    }

    private String findUserFolder(String username) {

        try {
            String url = alfrescoBaseUrl
                    + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                    + usersRootId
                    + "/children?maxItems=1000"; // adjust if you expect >1000

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", basicAuth());
            headers.setAccept(MediaType.parseMediaTypes("application/json"));

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode entries = root.path("list").path("entries");
            if (entries.isArray()) {
                for (JsonNode e : entries) {
                    JsonNode entry = e.path("entry");
                    String name = entry.path("name").asText("");
                    boolean isFolder = entry.path("isFolder").asBoolean(false);
                    if (isFolder && username.equals(name)) {
                        return entry.path("id").asText(null);
                    }
                }
            }
            return null;

        } catch (Exception e) {
            // log in real app
            return null;
        }

        // try {
        //     String url = alfrescoBaseUrl
        //             + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
        //             + usersRootId
        //             + "/children"
        //             + "?where=(name='" + username + "' AND isFolder=true)";

        //     HttpHeaders headers = new HttpHeaders();
        //     headers.set("Authorization", basicAuth());
        //     headers.setAccept(MediaType.parseMediaTypes("application/json"));

        //     ResponseEntity<String> response = restTemplate.exchange(
        //             url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        //     if (!response.getStatusCode().is2xxSuccessful()) {
        //         return null;
        //     }

        //     JsonNode root = objectMapper.readTree(response.getBody());
        //     JsonNode entries = root.path("list").path("entries");
        //     if (entries.isArray() && entries.size() > 0) {
        //         return entries.get(0).path("entry").path("id").asText(null);
        //     }
        //     return null;
        // } catch (Exception e) {
        //     // Log in real code
        //     return null;
        // }
    }

    private String createUserFolder(String username) {
        try {
            String url = alfrescoBaseUrl
                    + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                    + usersRootId
                    + "/children";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", basicAuth());
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(MediaType.parseMediaTypes("application/json"));

            String bodyJson = "{ \"name\": \"" + escape(username) + "\", \"nodeType\": \"cm:folder\" }";

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(bodyJson, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to create user folder: " + response.getBody());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("entry").path("id").asText(null);

        } catch (Exception e) {
            throw new RuntimeException("Error creating user folder", e);
        }
    }

    private String basicAuth() {
        String auth = alfrescoUsername + ":" + alfrescoPassword;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
