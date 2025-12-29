package com.companies.alfresco.dto;

public class DocumentStatusUpdateRequest {

    private String investorId;
    private String companyId;
    private String serviceId;
    private String fileName;

    // allowed: PENDING, APPROVED, REJECTED
    private String approvalStatus;

    // allowed: VERIFIED, UNVERIFIED
    private String verificationStatus;

    public DocumentStatusUpdateRequest() {}

    public String getInvestorId() { return investorId; }
    public void setInvestorId(String investorId) { this.investorId = investorId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
}
