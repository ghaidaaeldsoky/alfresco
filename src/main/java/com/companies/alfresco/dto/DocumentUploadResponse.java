package com.companies.alfresco.dto;

public class DocumentUploadResponse {
    private boolean success;
    private String message;

    private String folderId;
    private String nodeId;
    private String fileName;

    public DocumentUploadResponse() {}

    public DocumentUploadResponse(boolean success, String message, String folderId, String nodeId, String fileName) {
        this.success = success;
        this.message = message;
        this.folderId = folderId;
        this.nodeId = nodeId;
        this.fileName = fileName;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
