package com.companies.alfresco.dto;

import java.util.ArrayList;
import java.util.Map;

public class DocsRetrieveStatusResponse {
    private boolean success;
    private String message;
    private ArrayList<Map<String, Object>> documents;

    public DocsRetrieveStatusResponse() {}

    public DocsRetrieveStatusResponse(boolean success, String message, ArrayList<Map<String, Object>> documents) {
        this.success = success;
        this.message = message;
        this.documents = documents;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ArrayList<Map<String, Object>> getDocuments() { return documents; }
    public void setDocuments(ArrayList<Map<String, Object>> documents) { this.documents = documents; }
}
