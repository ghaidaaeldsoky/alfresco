package com.companies.alfresco.service;

import com.companies.alfresco.dto.DocFirstResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class DocsRetrieveFirstService {

    private final FolderHierarchyService hierarchyService;
    private final AlfrescoBrowseService browseService;
    private final AlfrescoContentService contentService;

    public DocsRetrieveFirstService(FolderHierarchyService hierarchyService,
                                    AlfrescoBrowseService browseService,
                                    AlfrescoContentService contentService) {
        this.hierarchyService = hierarchyService;
        this.browseService = browseService;
        this.contentService = contentService;
    }

    public DocFirstResponse retrieveFirst(String investorId, String companyId, String serviceId) {

        // 1) Ensure folder hierarchy exists and get the service folder ID
        String serviceFolderId = hierarchyService.ensureHierarchy(investorId, companyId, serviceId);

        // 2) List children in that folder
        JsonNode listJson = browseService.listChildrenJson(serviceFolderId);
        JsonNode entries = listJson.path("list").path("entries");

        if (!entries.isArray() || entries.size() == 0) {
            return new DocFirstResponse(false, "No documents found for this service folder");
        }

        // 3) Find first file entry
        JsonNode firstFileEntry = null;
        for (JsonNode e : entries) {
            JsonNode entry = e.path("entry");
            if (entry.path("isFile").asBoolean(false)) {
                firstFileEntry = entry;
                break;
            }
        }

        if (firstFileEntry == null) {
            return new DocFirstResponse(false, "Folder contains no files");
        }

        String nodeId = firstFileEntry.path("id").asText(null);
        String fileName = firstFileEntry.path("name").asText("document");
        String mimeType = firstFileEntry.path("content").path("mimeType").asText("application/octet-stream");
        long size = firstFileEntry.path("content").path("sizeInBytes").asLong(0);

        // 4) Download bytes from Alfresco
        ResponseEntity<byte[]> contentResp = contentService.getNodeContent(nodeId, false);
        byte[] bytes = contentResp.getBody() == null ? new byte[0] : contentResp.getBody();

        if (bytes.length == 0) {
            return new DocFirstResponse(false, "File content is empty or could not be retrieved");
        }

        // 5) Build response
        DocFirstResponse out = new DocFirstResponse(true, "OK");
        out.setNodeId(nodeId);
        out.setFileName(fileName);
        out.setMimeType(mimeType);
        out.setSize(bytes.length > 0 ? bytes.length : size);
        out.setContentBase64(Base64.getEncoder().encodeToString(bytes));
        return out;
    }
}
