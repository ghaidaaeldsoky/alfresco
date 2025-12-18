package com.companies.alfresco.dto;

import java.util.List;

public class DocsRetrieveResponse {
    private boolean success;
    private String message;
    private List<DocItemResponse> documents;

    public DocsRetrieveResponse() {}

    public DocsRetrieveResponse(boolean success, String message, List<DocItemResponse> documents) {
        this.success = success;
        this.message = message;
        this.documents = documents;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<DocItemResponse> getDocuments() { return documents; }
    public void setDocuments(List<DocItemResponse> documents) { this.documents = documents; }
}
