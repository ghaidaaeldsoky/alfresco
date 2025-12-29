package com.companies.alfresco.dto;

import java.util.List;

public class BulkDocumentStatusUpdateResponse {

    private boolean success;
    private String message;

    private List<String> allowedApprovalStatuses;
    private List<String> allowedVerificationStatuses;

    private List<DocumentStatusUpdateResult> results;

    public BulkDocumentStatusUpdateResponse() {}

    public BulkDocumentStatusUpdateResponse(boolean success, String message,
                                            List<String> allowedApprovalStatuses,
                                            List<String> allowedVerificationStatuses,
                                            List<DocumentStatusUpdateResult> results) {
        this.success = success;
        this.message = message;
        this.allowedApprovalStatuses = allowedApprovalStatuses;
        this.allowedVerificationStatuses = allowedVerificationStatuses;
        this.results = results;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getAllowedApprovalStatuses() { return allowedApprovalStatuses; }
    public void setAllowedApprovalStatuses(List<String> allowedApprovalStatuses) { this.allowedApprovalStatuses = allowedApprovalStatuses; }

    public List<String> getAllowedVerificationStatuses() { return allowedVerificationStatuses; }
    public void setAllowedVerificationStatuses(List<String> allowedVerificationStatuses) { this.allowedVerificationStatuses = allowedVerificationStatuses; }

    public List<DocumentStatusUpdateResult> getResults() { return results; }
    public void setResults(List<DocumentStatusUpdateResult> results) { this.results = results; }
}
