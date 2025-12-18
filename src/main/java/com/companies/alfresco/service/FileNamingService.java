package com.companies.alfresco.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileNamingService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String buildFileName(String docType, String serviceId, String companyId, String originalFileName) {
        String ext = extractExt(originalFileName);
        String ts = LocalDateTime.now().format(TS);

        // String safeDocType = sanitize(docType);
        String safeService = sanitize(serviceId);
        String safeCompany = sanitize(companyId);

        // return safeDocType + "_" + safeService + "_" + safeCompany + "_" + ts + ext;
        return safeService + "_" + safeCompany + "_" + ts + ext;
    }

    private String extractExt(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx == -1) return "";
        return name.substring(idx);
    }

    private String sanitize(String s) {
        if (s == null) return "NA";
        // remove characters that break paths or OS
        return s.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }
}
