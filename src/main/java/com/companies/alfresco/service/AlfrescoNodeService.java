package com.companies.alfresco.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AlfrescoNodeService {

    private final RestTemplate restTemplate;
    private final AlfrescoAuthService auth;
    private final ObjectMapper mapper = new ObjectMapper();

    private final String alfrescoBaseUrl;

    public AlfrescoNodeService(RestTemplate restTemplate, AlfrescoAuthService auth, @Value("${alfresco.base-url}")String alfrescoBaseUrl ) {
        this.restTemplate = restTemplate;
        this.auth = auth;
        this.alfrescoBaseUrl = alfrescoBaseUrl;
    }

    /** Ensure root folder "User Documents" exists under -root- and return its nodeId */
    public String getOrCreateUserDocumentsRoot() {
        String rootId = "-root-";
        String folderName = "User Documents";
        return createOrGetFolder(rootId, folderName);
    }

    /** Create folder under parentId. If 409 duplicate => find it and return id. */
    public String createOrGetFolder(String parentId, String folderName) {
        try {
            return createFolder(parentId, folderName);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String id = findChildFolderIdByQuery(parentId, folderName); //changed
                if (id != null) return id;
                throw new RuntimeException("Got 409 but could not find existing folder: " + folderName);
            }
            throw e;
        }
    }

    /** POST /nodes/{parentId}/children { name, nodeType=cm:folder } */
    public String createFolder(String parentId, String folderName) {
        String url = alfrescoBaseUrl + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + parentId + "/children";

        HttpHeaders headers = auth.authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        String body = "{ \"name\": \"" + escape(folderName) + "\", \"nodeType\": \"cm:folder\" }";

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);

        try {
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("entry").path("id").asText(null);
        } catch (Exception ex) {
            throw new RuntimeException("Failed parsing folder create response", ex);
        }
    }

    /**
     * Use queries API to find exact folderName under parent (rootNodeId).
     * GET /queries/nodes?term=...&rootNodeId=...&nodeType=cm:folder
     */
    public String findChildFolderIdByQuery(String parentId, String folderName) {
        try {
            String term = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
            String url = alfrescoBaseUrl + "/alfresco/api/-default-/public/alfresco/versions/1/queries/nodes"
                    + "?term=" + term
                    + "&rootNodeId=" + parentId
                    + "&nodeType=cm:folder";

            HttpHeaders headers = auth.authHeaders();
            headers.setAccept(MediaType.parseMediaTypes("application/json"));

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode entries = root.path("list").path("entries");
            if (entries.isArray()) {
                for (JsonNode e : entries) {
                    JsonNode entry = e.path("entry");
                    boolean isFolder = entry.path("isFolder").asBoolean(false);
                    String name = entry.path("name").asText("");
                    if (isFolder && folderName.equals(name)) {
                        return entry.path("id").asText(null);
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }


public String findChildFolderId(String parentId, String folderName) {
    try {
        String url = UriComponentsBuilder
                .fromPath(alfrescoBaseUrl + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + parentId + "/children")
                .queryParam("where", "(isFolder=true AND name='" + folderName.replace("'", "\\'") + "')")
                .queryParam("maxItems", "100")
                .build(false)   // IMPORTANT: don't double encode
                .toUriString();

        HttpHeaders headers = auth.authHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        JsonNode root = mapper.readTree(response.getBody());
        JsonNode entries = root.path("list").path("entries");

        if (entries.isArray() && entries.size() > 0) {
            return entries.get(0).path("entry").path("id").asText(null);
        }
        return null;

    } catch (Exception e) {
        return null;
    }
}

}
