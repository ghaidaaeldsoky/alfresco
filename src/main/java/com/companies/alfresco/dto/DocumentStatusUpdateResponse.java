package com.companies.alfresco.dto;

public class DocumentStatusUpdateResponse {

    private boolean success;
    private String message;

    private String nodeId;
    private String fileName;

    // NEW: final persisted values
    private String approvalStatus;
    private String verificationStatus;

    public DocumentStatusUpdateResponse() {}

    public DocumentStatusUpdateResponse(boolean success,
                                        String message,
                                        String nodeId,
                                        String fileName,
                                        String approvalStatus,
                                        String verificationStatus) {
        this.success = success;
        this.message = message;
        this.nodeId = nodeId;
        this.fileName = fileName;
        this.approvalStatus = approvalStatus;
        this.verificationStatus = verificationStatus;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
}
