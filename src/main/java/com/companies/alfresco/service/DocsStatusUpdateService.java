package com.companies.alfresco.service;

import com.companies.alfresco.dto.DocumentStatusUpdateRequest;
import com.companies.alfresco.dto.DocumentStatusUpdateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DocsStatusUpdateService {

    private final FolderHierarchyService hierarchyService;
    private final AlfrescoBrowseService browseService;
    private final RestTemplate restTemplate;
    private final AlfrescoAuthService authService;

    private final ObjectMapper mapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${alfresco.base-url}")
    private String alfrescoBaseUrl;

    public DocsStatusUpdateService(FolderHierarchyService hierarchyService,
                                   AlfrescoBrowseService browseService,
                                   RestTemplate restTemplate,
                                   AlfrescoAuthService authService) {
        this.hierarchyService = hierarchyService;
        this.browseService = browseService;
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    public DocumentStatusUpdateResponse updateStatus(DocumentStatusUpdateRequest req) {
        validate(req);

        // 1) Ensure folder hierarchy exists + get service folder id
        String serviceFolderId = hierarchyService.ensureHierarchy(
                req.getInvestorId(), req.getCompanyId(), req.getServiceId()
        );

        // 2) Find nodeId by exact filename inside that folder
        String nodeId = findFileNodeIdByName(serviceFolderId, req.getFileName());
        if (nodeId == null) {
            return new DocumentStatusUpdateResponse(false,
                    "File not found in service folder: " + req.getFileName(),
                    null,
                    req.getFileName(),
                    null,
                    null);
        }

        // 3) Update properties on the node (Alfresco PUT /nodes/{id})
        updateAlfrescoProperties(nodeId, req.getApprovalStatus(), req.getVerificationStatus());

        return new DocumentStatusUpdateResponse(
        true,
        "Updated document status successfully",
        nodeId,
        req.getFileName(),
        req.getApprovalStatus(),
        req.getVerificationStatus()
);
    }

    private void validate(DocumentStatusUpdateRequest req) {
        if (blank(req.getInvestorId()) || blank(req.getCompanyId()) || blank(req.getServiceId()) || blank(req.getFileName())) {
            throw new IllegalArgumentException("investorId/companyId/serviceId/fileName are required");
        }
        if (blank(req.getApprovalStatus()) || blank(req.getVerificationStatus())) {
            throw new IllegalArgumentException("approvalStatus and verificationStatus are required");
        }

        Set<String> approvalAllowed = new HashSet<>(Arrays.asList("PENDING", "APPROVED", "REJECTED"));
        Set<String> verifyAllowed = new HashSet<>(Arrays.asList("VERIFIED", "UNVERIFIED"));

        if (!approvalAllowed.contains(req.getApprovalStatus())) {
            throw new IllegalArgumentException("approvalStatus must be one of " + approvalAllowed);
        }
        if (!verifyAllowed.contains(req.getVerificationStatus())) {
            throw new IllegalArgumentException("verificationStatus must be one of " + verifyAllowed);
        }
    }

    private String findFileNodeIdByName(String folderId, String fileName) {
        JsonNode root = browseService.listChildrenJson(folderId);
        JsonNode entries = root.path("list").path("entries");

        if (!entries.isArray()) return null;

        for (JsonNode e : entries) {
            JsonNode entry = e.path("entry");
            boolean isFile = entry.path("isFile").asBoolean(false);
            String name = entry.path("name").asText("");
            if (isFile && fileName.equals(name)) {
                return entry.path("id").asText(null);
            }
        }
        return null;
    }

    private void updateAlfrescoProperties(String nodeId, String approvalStatus, String verificationStatus) {
        String url = alfrescoBaseUrl
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + nodeId;

        // request body exactly as Alfresco expects for metadata update
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> props = new HashMap<>();
        props.put("cp:approvalStatus", approvalStatus);
        props.put("cp:verificationStatus", verificationStatus);
        body.put("properties", props);

        HttpHeaders headers = authService.authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed updating Alfresco node properties: " + resp.getBody());
        }
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
