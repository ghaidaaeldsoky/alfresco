package com.companies.alfresco.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        // Create folder 
        return createUserFolder(username);
    }

    private String findUserFolder(String username) {

        try {

            String encodedTerm = URLEncoder.encode(username, StandardCharsets.UTF_8.name());

            String url = alfrescoBaseUrl
                    + "/alfresco/api/-default-/public/alfresco/versions/1/queries/nodes"
                    + "?term=" + encodedTerm
                    + "&rootNodeId=" + usersRootId
                    + "&nodeType=cm:folder";

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

        } catch (HttpClientErrorException e) {
            // 409 = Duplicate child name not allowed -> folder already exists
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String existingId = findUserFolder(username);
                if (existingId != null) {
                    return existingId;
                }
                // If we got 409 but cannot find it, something is inconsistent:
                throw new RuntimeException("Folder already exists but cannot be found for username: " + username, e);
            }
            throw new RuntimeException("Error creating user folder", e);
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
