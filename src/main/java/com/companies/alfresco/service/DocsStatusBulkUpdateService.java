package com.companies.alfresco.service;

import com.companies.alfresco.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DocsStatusBulkUpdateService {

    private static final List<String> APPROVAL_STATUSES = List.of("PENDING", "APPROVED", "REJECTED");
    private static final List<String> VERIFICATION_STATUSES = List.of("VERIFIED", "UNVERIFIED");

    private final FolderHierarchyService hierarchyService;
    private final AlfrescoBrowseService browseService;
    private final RestTemplate restTemplate;
    private final AlfrescoAuthService authService;

    @org.springframework.beans.factory.annotation.Value("${alfresco.base-url}")
    private String alfrescoBaseUrl;

    public DocsStatusBulkUpdateService(FolderHierarchyService hierarchyService,
                                       AlfrescoBrowseService browseService,
                                       RestTemplate restTemplate,
                                       AlfrescoAuthService authService) {
        this.hierarchyService = hierarchyService;
        this.browseService = browseService;
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    public BulkDocumentStatusUpdateResponse bulkUpdate(BulkDocumentStatusUpdateRequest req) {
        validate(req);

        // 1) Ensure folder hierarchy exists + get folderId
        String serviceFolderId = hierarchyService.ensureHierarchy(req.getInvestorId(), req.getCompanyId(), req.getServiceId());

        // 2) List children once (performance) and map name -> nodeId
        Map<String, String> fileNameToNodeId = buildFileIndex(serviceFolderId);

        // 3) Update each requested file
        List<DocumentStatusUpdateResult> results = new ArrayList<>();
        for (DocumentStatusItem item : req.getDocuments()) {
            String fileName = item.getFileName();
            String nodeId = fileNameToNodeId.get(fileName);

            if (nodeId == null) {
                results.add(new DocumentStatusUpdateResult(fileName, null, false, "File not found in service folder"));
                continue;
            }

            try {
                updateAlfrescoProperties(nodeId, item.getApprovalStatus(), item.getVerificationStatus(), item.getReason());
                results.add(new DocumentStatusUpdateResult(fileName, nodeId, true, "Updated"));
            } catch (Exception ex) {
                results.add(new DocumentStatusUpdateResult(fileName, nodeId, false, "Update failed: " + ex.getMessage()));
            }
        }

        return new BulkDocumentStatusUpdateResponse(
                true,
                "Bulk update processed",
                APPROVAL_STATUSES,
                VERIFICATION_STATUSES,
                results
        );
    }

    private void validate(BulkDocumentStatusUpdateRequest req) {
        if (blank(req.getInvestorId()) || blank(req.getCompanyId()) || blank(req.getServiceId())) {
            throw new IllegalArgumentException("investorId/companyId/serviceId are required");
        }
        if (req.getDocuments() == null || req.getDocuments().isEmpty()) {
            throw new IllegalArgumentException("documents list is required");
        }

        for (DocumentStatusItem item : req.getDocuments()) {
            if (item == null || blank(item.getFileName())) {
                throw new IllegalArgumentException("Each document item must include fileName");
            }
            if (blank(item.getApprovalStatus()) || !APPROVAL_STATUSES.contains(item.getApprovalStatus())) {
                throw new IllegalArgumentException("approvalStatus must be one of " + APPROVAL_STATUSES + " for file " + item.getFileName());
            }
            if (blank(item.getVerificationStatus()) || !VERIFICATION_STATUSES.contains(item.getVerificationStatus())) {
                throw new IllegalArgumentException("verificationStatus must be one of " + VERIFICATION_STATUSES + " for file " + item.getFileName());
            }
        }
    }

    private Map<String, String> buildFileIndex(String folderId) {
        JsonNode root = browseService.listChildrenJson(folderId);
        JsonNode entries = root.path("list").path("entries");

        Map<String, String> map = new HashMap<>();
        if (entries.isArray()) {
            for (JsonNode e : entries) {
                JsonNode entry = e.path("entry");
                if (entry.path("isFile").asBoolean(false)) {
                    String name = entry.path("name").asText("");
                    String id = entry.path("id").asText(null);
                    if (!name.isEmpty() && id != null) {
                        map.put(name, id);
                    }
                }
            }
        }
        return map;
    }

    private void updateAlfrescoProperties(String nodeId, String approvalStatus, String verificationStatus, String reason) {
        String url = alfrescoBaseUrl
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + nodeId;

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("cp:approvalStatus", approvalStatus);
        props.put("cp:verificationStatus", verificationStatus);

        // Optional: store reason if you created a property for it (example cp:reviewReason)
        if (reason != null && !reason.trim().isEmpty()) {
            props.put("cp:reviewReason", reason);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("properties", props);

        HttpHeaders headers = authService.authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Alfresco update failed: " + resp.getBody());
        }
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
