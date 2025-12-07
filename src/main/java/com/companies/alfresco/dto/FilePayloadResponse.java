package com.companies.alfresco.dto;

public class FilePayloadResponse {

    private String message;
    private boolean success;
    private long receivedSize;
    private String alfrescoResponseJson; // full raw JSON (optional, for debugging)

    public FilePayloadResponse() {}

    public FilePayloadResponse(String message,
                               boolean success,
                               long receivedSize,
                               String alfrescoResponseJson) {
        this.message = message;
        this.success = success;
        this.receivedSize = receivedSize;;
        this.alfrescoResponseJson = alfrescoResponseJson;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public long getReceivedSize() { return receivedSize; }
    public void setReceivedSize(long receivedSize) { this.receivedSize = receivedSize; }

    public String getAlfrescoResponseJson() { return alfrescoResponseJson; }
    public void setAlfrescoResponseJson(String alfrescoResponseJson) { this.alfrescoResponseJson = alfrescoResponseJson; }
}
