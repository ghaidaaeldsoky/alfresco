package com.companies.alfresco.dto;

import java.util.List;

public class BulkDocumentStatusUpdateRequest {
    private String investorId;
    private String companyId;
    private String serviceId;

    private List<DocumentStatusItem> documents;

    public BulkDocumentStatusUpdateRequest() {}

    public String getInvestorId() { return investorId; }
    public void setInvestorId(String investorId) { this.investorId = investorId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public List<DocumentStatusItem> getDocuments() { return documents; }
    public void setDocuments(List<DocumentStatusItem> documents) { this.documents = documents; }
}
