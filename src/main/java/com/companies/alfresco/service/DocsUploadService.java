package com.companies.alfresco.service;

import com.companies.alfresco.dto.DocumentUploadRequest;
import com.companies.alfresco.dto.DocumentUploadResponse;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class DocsUploadService {

    private final FolderHierarchyService hierarchyService;
    private final FileNamingService namingService;
    private final AlfrescoDocumentService alfrescoDocumentService;

    public DocsUploadService(FolderHierarchyService hierarchyService,
                             FileNamingService namingService,
                             AlfrescoDocumentService alfrescoDocumentService) {
        this.hierarchyService = hierarchyService;
        this.namingService = namingService;
        this.alfrescoDocumentService = alfrescoDocumentService;
    }

    public DocumentUploadResponse upload(DocumentUploadRequest req) {

        validate(req);

        // 1) Ensure hierarchy and get service folderId
        String folderId = hierarchyService.ensureHierarchy(req.getInvestorId(), req.getCompanyId(), req.getServiceId());

        // 2) Decode base64
        byte[] bytes = decodeBase64(req.getContentBase64());

        // 3) Validate size
        if (req.getSize() > 0 && bytes.length != req.getSize()) {
            return new DocumentUploadResponse(false,
                    "Size mismatch: expected " + req.getSize() + ", got " + bytes.length,
                    folderId, null, null);
        }

        // 4) Build final filename
        String finalName = namingService.buildFileName("DOC",req.getServiceId(),req.getCompanyId(),req.getOriginalFileName());

        // 5) Mime type fallback
        String mimeType = (req.getMimeType() == null || req.getMimeType().trim().isEmpty())
                ? "application/octet-stream"
                : req.getMimeType();

        // 6) Upload to Alfresco + store serviceId in metadata
        AlfrescoDocumentService.UploadResult result =
                alfrescoDocumentService.upload(folderId, finalName, bytes, mimeType, req.getServiceId());

        if (result == null || result.nodeId == null) {
            return new DocumentUploadResponse(false,
                    "Upload failed: Alfresco did not return nodeId",
                    folderId, null, finalName);
        }

        return new DocumentUploadResponse(true,
                "Uploaded successfully",
                folderId,
                result.nodeId,
                finalName);
    }

    // ---------------- helpers ----------------

    private void validate(DocumentUploadRequest req) {
        if (req == null) throw new IllegalArgumentException("Request is null");

        require(req.getInvestorId(), "investorId");
        require(req.getCompanyId(), "companyId");
        require(req.getServiceId(), "serviceId");

        require(req.getOriginalFileName(), "originalFileName");
        require(req.getContentBase64(), "contentBase64");

        if (req.getSize() <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
    }

    private void require(String v, String name) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }

    private byte[] decodeBase64(String b64) {
        try {
            return Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 content", e);
        }
    }
}
