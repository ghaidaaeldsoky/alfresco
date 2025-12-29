package com.companies.alfresco.dto;

public class DocumentStatusUpdateResult {
    private String fileName;
    private String nodeId;
    private boolean success;
    private String message;

    public DocumentStatusUpdateResult() {}

    public DocumentStatusUpdateResult(String fileName, String nodeId, boolean success, String message) {
        this.fileName = fileName;
        this.nodeId = nodeId;
        this.success = success;
        this.message = message;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
