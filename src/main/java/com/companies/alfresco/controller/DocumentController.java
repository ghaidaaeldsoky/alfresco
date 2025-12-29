package com.companies.alfresco.controller;

import com.companies.alfresco.dto.DocFirstResponse;
import com.companies.alfresco.dto.DocsRetrieveRequest;
import com.companies.alfresco.dto.DocsRetrieveResponse;
import com.companies.alfresco.dto.DocumentStatusUpdateRequest;
import com.companies.alfresco.dto.DocumentStatusUpdateResponse;
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
    private final DocsRetrieveFirstService docsRetrieveFirstService;
    private final DocsListNamesService docsListNamesService;
    private final DocsStatusUpdateService docsStatusUpdateService;

    public DocumentController(DocsUploadService docsUploadService, DocsRetrieveService docsRetrieveService, DocsRetrieveFirstService docsRetrieveFirstService,
        DocsListNamesService docsListNamesService , DocsStatusUpdateService docsStatusUpdateService
    ) {
        this.docsUploadService = docsUploadService;
        this.docsRetrieveService = docsRetrieveService;
        this.docsRetrieveFirstService = docsRetrieveFirstService;
        this.docsListNamesService = docsListNamesService;
        this.docsStatusUpdateService = docsStatusUpdateService;
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

@PostMapping("/retrieve-first")
    public ResponseEntity<DocFirstResponse> retrieveFirst(@RequestBody DocsRetrieveRequest req) {
        if (req.getInvestorId() == null || req.getInvestorId().isBlank()
                || req.getCompanyId() == null || req.getCompanyId().isBlank()
                || req.getServiceId() == null || req.getServiceId().isBlank()) {
            return ResponseEntity.badRequest().body(new DocFirstResponse(false, "investorId/companyId/serviceId are required"));
        }

        return ResponseEntity.ok(
                docsRetrieveFirstService.retrieveFirst(req.getInvestorId(), req.getCompanyId(), req.getServiceId())
        );
    }

    @PostMapping("/list-names")
public ResponseEntity<com.companies.alfresco.dto.DocsListNamesResponse> listNames(
        @RequestBody DocsRetrieveRequest req) {

    if (req.getInvestorId() == null || req.getInvestorId().isBlank()
            || req.getCompanyId() == null || req.getCompanyId().isBlank()
            || req.getServiceId() == null || req.getServiceId().isBlank()) {
        return ResponseEntity.badRequest().body(
                new com.companies.alfresco.dto.DocsListNamesResponse(false,
                        "investorId/companyId/serviceId are required", java.util.Collections.emptyList())
        );
    }

    return ResponseEntity.ok(
            docsListNamesService.listFileNames(req.getInvestorId(), req.getCompanyId(), req.getServiceId())
    );   
}

@PostMapping("/update-status")
public ResponseEntity<DocumentStatusUpdateResponse> updateStatus(
        @RequestBody DocumentStatusUpdateRequest req) {

    try {
        return ResponseEntity.ok(docsStatusUpdateService.updateStatus(req));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                new com.companies.alfresco.dto.DocumentStatusUpdateResponse(false, e.getMessage(), null, req.getFileName(),req.getApprovalStatus(), req.getVerificationStatus())
        );
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(
                new com.companies.alfresco.dto.DocumentStatusUpdateResponse(false, "Server error: " + e.getMessage(), null, req.getFileName(), null, null)
        );
    }
}

}
