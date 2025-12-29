package com.companies.alfresco.dto;

public class DocumentStatusItem {

    private String fileName;
    private String approvalStatus;      // PENDING/APPROVED/REJECTED
    private String verificationStatus;  // VERIFIED/UNVERIFIED
    private String reason;              // optional

    public DocumentStatusItem() {}

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
