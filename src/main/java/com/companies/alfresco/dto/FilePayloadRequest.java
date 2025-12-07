package com.companies.alfresco.dto;


public class FilePayloadRequest {
    private String fileName;
    private String contentBase64;
    private long size;

    private String username;       // NEW – logical Alfresco owner
    private String parentFolderId; // e.g. "-root-" or folder node id
    private String mimeType;       // e.g. "application/pdf"

    public FilePayloadRequest() {}

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentBase64() { return contentBase64; }
    public void setContentBase64(String contentBase64) { this.contentBase64 = contentBase64; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(String parentFolderId) { this.parentFolderId = parentFolderId; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getUsername() { return username;}
    public void setUsername(String username) { this.username = username;}
    
}
