package com.companies.alfresco.controller;

import com.companies.alfresco.dto.DocsRetrieveRequest;
import com.companies.alfresco.dto.DocsRetrieveResponse;
import com.companies.alfresco.dto.DocumentUploadRequest;
import com.companies.alfresco.dto.DocumentUploadResponse;
import com.companies.alfresco.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    private final DocsUploadService docsUploadService;
    private final DocsRetrieveService docsRetrieveService;

    public DocumentController(DocsUploadService docsUploadService, DocsRetrieveService docsRetrieveService) {
        this.docsUploadService = docsUploadService;
        this.docsRetrieveService = docsRetrieveService;
    }

    @PostMapping   // ("/upload")
    public ResponseEntity<DocumentUploadResponse> upload(@RequestBody DocumentUploadRequest req) {
        try {
            return ResponseEntity.ok(docsUploadService.upload(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new DocumentUploadResponse(false, e.getMessage(), null, null, null)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new DocumentUploadResponse(false, "Server error: " + e.getMessage(), null, null, null)
            );
        }
    }

    @PostMapping("/retrieve-all")
    public ResponseEntity<DocsRetrieveResponse> retrieveAll(
        @RequestBody DocsRetrieveRequest req) {

        if (req.getInvestorId() == null || req.getCompanyId() == null || req.getServiceId() == null) {
            return ResponseEntity.badRequest().body(
                    new com.companies.alfresco.dto.DocsRetrieveResponse(false, "investorId/companyId/serviceId required", null)
            );
        }

        return ResponseEntity.ok(
                docsRetrieveService.retrieveAll(req.getInvestorId(), req.getCompanyId(), req.getServiceId())
        );
}


    // private final FolderHierarchyService hierarchyService;
    // private final FileNamingService namingService;
    // private final AlfrescoDocumentService documentService;
    // private final AlfrescoBrowseService browseService;
    // private final AlfrescoContentService contentService;

    // public DocumentController(FolderHierarchyService hierarchyService,
    //                           FileNamingService namingService,
    //                           AlfrescoDocumentService documentService,
    //                           AlfrescoBrowseService browseService,
    //                           AlfrescoContentService contentService) {
    //     this.hierarchyService = hierarchyService;
    //     this.namingService = namingService;
    //     this.documentService = documentService;
    //     this.browseService = browseService;
    //     this.contentService = contentService;
    // }

    // // POST /api/docs
    // @PostMapping
    // public ResponseEntity<DocumentUploadResponse> upload(@RequestBody DocumentUploadRequest req) {
    //     try {
    //         if (blank(req.getInvestorId()) || blank(req.getCompanyId()) || blank(req.getServiceId())) {
    //             return ResponseEntity.badRequest().body(
    //                     new DocumentUploadResponse(false, "investorId/companyId/serviceId are required",
    //                             null, null, null));
    //         }
    //         if (blank(req.getOriginalFileName()) || blank(req.getContentBase64()) || req.getSize() <= 0) {
    //             return ResponseEntity.badRequest().body(
    //                     new DocumentUploadResponse(false, "originalFileName/contentBase64/size are required",
    //                             null, null, null));
    //         }

    //         String folderId = hierarchyService.ensureHierarchy(req.getInvestorId(), req.getCompanyId(), req.getServiceId());

    //         byte[] bytes = Base64.getDecoder().decode(req.getContentBase64());
    //         if (bytes.length != req.getSize()) {
    //             return ResponseEntity.badRequest().body(
    //                     new DocumentUploadResponse(false,
    //                             "Size mismatch: expected " + req.getSize() + " got " + bytes.length,
    //                             folderId, null, null));
    //         }

    //         String fileName = namingService.buildFileName("DOC",req.getServiceId(),req.getCompanyId(),req.getOriginalFileName());
    //         String mimeType = blank(req.getMimeType()) ? "application/octet-stream" : req.getMimeType();

    //         AlfrescoDocumentService.UploadResult result =
    //                 documentService.upload(folderId, fileName, bytes, mimeType, req.getServiceId());

    //         return ResponseEntity.ok(new DocumentUploadResponse(
    //                 true, "Uploaded successfully", folderId, result.nodeId, fileName));

    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().body(
    //                 new DocumentUploadResponse(false, "Error: " + e.getMessage(), null, null, null));
    //     }
    // }

    // GET /api/docs/list?investorId=..&companyId=..&serviceId=..
    // @GetMapping("/list")
    // public ResponseEntity<String> list(@RequestParam String investorId,
    //                                    @RequestParam String companyId,
    //                                    @RequestParam String serviceId) {
    //     String folderId = hierarchyService.ensureHierarchy(investorId, companyId, serviceId);
    //     return ResponseEntity.ok(browseService.listChildrenByFolderId(folderId));
    // }

    // // GET /api/docs/content/{nodeId}?attachment=false
    // @GetMapping("/content/{nodeId}")
    // public ResponseEntity<byte[]> content(@PathVariable String nodeId,
    //                                       @RequestParam(defaultValue = "false") boolean attachment) {
    //     return contentService.getNodeContent(nodeId, attachment);
    // }

    // private boolean blank(String s) {
    //     return s == null || s.trim().isEmpty();
    // }
}
