package com.companies.alfresco.dto;

import java.util.List;

public class DocsListNamesResponse {
    private boolean success;
    private String message;
    private List<String> fileNames;

    public DocsListNamesResponse() {}

    public DocsListNamesResponse(boolean success, String message, List<String> fileNames) {
        this.success = success;
        this.message = message;
        this.fileNames = fileNames;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getFileNames() { return fileNames; }
    public void setFileNames(List<String> fileNames) { this.fileNames = fileNames; }
}
