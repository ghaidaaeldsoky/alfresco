package com.companies.alfresco.dto;

public class DocsRetrieveRequest {
    private String investorId;
    private String companyId;
    private String serviceId;

    public DocsRetrieveRequest() {}

    public String getInvestorId() { return investorId; }
    public void setInvestorId(String investorId) { this.investorId = investorId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
}
