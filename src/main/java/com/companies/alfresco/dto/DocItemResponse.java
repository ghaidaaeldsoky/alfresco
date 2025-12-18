package com.companies.alfresco.dto;

public class DocItemResponse {
    private String nodeId;
    private String fileName;
    private String mimeType;
    private long size;
    private String contentBase64;

    public DocItemResponse() {}

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getContentBase64() { return contentBase64; }
    public void setContentBase64(String contentBase64) { this.contentBase64 = contentBase64; }
}
