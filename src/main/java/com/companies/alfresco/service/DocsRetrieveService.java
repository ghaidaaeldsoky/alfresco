package com.companies.alfresco.service;

import com.companies.alfresco.dto.DocItemResponse;
import com.companies.alfresco.dto.DocsRetrieveResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class DocsRetrieveService {

    private final FolderHierarchyService hierarchyService;
    private final AlfrescoBrowseService browseService;
    private final AlfrescoContentService contentService;

    public DocsRetrieveService(FolderHierarchyService hierarchyService,
                               AlfrescoBrowseService browseService,
                               AlfrescoContentService contentService) {
        this.hierarchyService = hierarchyService;
        this.browseService = browseService;
        this.contentService = contentService;
    }

    public DocsRetrieveResponse retrieveAll(String investorId, String companyId, String serviceId) {

        // Ensure folders exist and get service folderId
        String serviceFolderId = hierarchyService.ensureHierarchy(investorId, companyId, serviceId);

        // List children
        JsonNode listJson = browseService.listChildrenJson(serviceFolderId);
        JsonNode entries = listJson.path("list").path("entries");

        List<DocItemResponse> out = new ArrayList<>();

        if (entries.isArray()) {
            for (JsonNode e : entries) {
                JsonNode entry = e.path("entry");
                boolean isFile = entry.path("isFile").asBoolean(false);
                if (!isFile) continue;

                String nodeId = entry.path("id").asText(null);
                String name = entry.path("name").asText("document");
                String mimeType = entry.path("content").path("mimeType").asText("application/octet-stream");
                long size = entry.path("content").path("sizeInBytes").asLong(0);

                // Download content bytes
                ResponseEntity<byte[]> contentResp = contentService.getNodeContent(nodeId, false);
                byte[] bytes = contentResp.getBody() == null ? new byte[0] : contentResp.getBody();

                DocItemResponse item = new DocItemResponse();
                item.setNodeId(nodeId);
                item.setFileName(name);
                item.setMimeType(mimeType);
                item.setSize(bytes.length > 0 ? bytes.length : size);
                item.setContentBase64(Base64.getEncoder().encodeToString(bytes));

                out.add(item);
            }
        }

        return new DocsRetrieveResponse(true, "OK", out);
    }
}
