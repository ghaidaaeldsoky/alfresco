package com.companies.alfresco.dto;

public class DocumentUploadRequest {
    private String investorId;
    private String companyId;
    private String serviceId;

    private String originalFileName;
    private String contentBase64;
    private long size;
    private String mimeType;

    public DocumentUploadRequest() {}

    public String getInvestorId() { return investorId; }
    public void setInvestorId(String investorId) { this.investorId = investorId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getContentBase64() { return contentBase64; }
    public void setContentBase64(String contentBase64) { this.contentBase64 = contentBase64; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
