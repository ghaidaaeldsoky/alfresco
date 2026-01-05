package com.companies.alfresco.service;

import com.companies.alfresco.dto.DocsRetrieveStatusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DocsRetrieveStatusService {

    private final FolderHierarchyService hierarchyService;
    private final AlfrescoBrowseService browseService;
    private final RestTemplate restTemplate;
    private final AlfrescoAuthService authService;

    private final ObjectMapper mapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${alfresco.base-url}")
    private String alfrescoBaseUrl;

    public DocsRetrieveStatusService(FolderHierarchyService hierarchyService,
                                    AlfrescoBrowseService browseService,
                                    RestTemplate restTemplate,
                                    AlfrescoAuthService authService) {
        this.hierarchyService = hierarchyService;
        this.browseService = browseService;
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    public DocsRetrieveStatusResponse retrieveStatuses(String investorId, String companyId, String serviceId) {

        if (blank(investorId) || blank(companyId) || blank(serviceId)) {
            throw new IllegalArgumentException("investorId/companyId/serviceId are required");
        }

        // 1) Ensure folders exist and get SRV folder id
        String serviceFolderId = hierarchyService.ensureHierarchy(investorId, companyId, serviceId);

        // 2) List children (files)
        JsonNode childrenJson = browseService.listChildrenJson(serviceFolderId);
        JsonNode entries = childrenJson.path("list").path("entries");

        ArrayList<Map<String, Object>> docs = new ArrayList<>();

        if (entries.isArray()) {
            for (JsonNode e : entries) {
                JsonNode entry = e.path("entry");
                boolean isFile = entry.path("isFile").asBoolean(false);
                if (!isFile) continue;

                String nodeId = entry.path("id").asText(null);
                String fileName = entry.path("name").asText("");

                if (nodeId == null) continue;

                // 3) Get node properties
                Map<String, Object> props = fetchNodeProperties(nodeId);

                // Build map as requested
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("fileName", fileName);
                m.put("nodeId", nodeId);

                m.put("approvalStatus", props.get("cp:approvalStatus"));
                m.put("verificationStatus", props.get("cp:verificationStatus"));
                m.put("isFinal", props.get("cp:isFinal"));
                m.put("reason", props.get("cp:reviewReason")); // change name if your model uses different prop

                docs.add(m);
            }
        }

        if (docs.isEmpty()) {
            return new DocsRetrieveStatusResponse(false, "No files found in this service folder", docs);
        }

        return new DocsRetrieveStatusResponse(true, "OK", docs);
    }

    private Map<String, Object> fetchNodeProperties(String nodeId) {
        String url = alfrescoBaseUrl
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + nodeId;

        HttpHeaders headers = authService.authHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            return Collections.emptyMap();
        }

        try {
            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode properties = root.path("entry").path("properties");

            // Convert JsonNode properties to Map<String,Object>
            if (properties == null || properties.isMissingNode()) {
                return Collections.emptyMap();
            }

            Map<String, Object> map = new HashMap<>();
            Iterator<String> fieldNames = properties.fieldNames();
            while (fieldNames.hasNext()) {
                String f = fieldNames.next();
                JsonNode v = properties.get(f);

                // simple conversions
                if (v.isBoolean()) map.put(f, v.asBoolean());
                else if (v.isNumber()) map.put(f, v.numberValue());
                else if (v.isNull()) map.put(f, null);
                else map.put(f, v.asText());
            }
            return map;

        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
