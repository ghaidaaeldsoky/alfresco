package com.companies.alfresco.service;

import com.companies.alfresco.dto.DocsListNamesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocsListNamesService {

    private final FolderHierarchyService hierarchyService;
    private final AlfrescoBrowseService browseService;

    public DocsListNamesService(FolderHierarchyService hierarchyService,
                                AlfrescoBrowseService browseService) {
        this.hierarchyService = hierarchyService;
        this.browseService = browseService;
    }

    public DocsListNamesResponse listFileNames(String investorId, String companyId, String serviceId) {

        // Ensure folder exists + get service folder id
        String serviceFolderId = hierarchyService.ensureHierarchy(investorId, companyId, serviceId);

        JsonNode listJson = browseService.listChildrenJson(serviceFolderId);
        JsonNode entries = listJson.path("list").path("entries");

        List<String> names = new ArrayList<>();
        if (entries.isArray()) {
            for (JsonNode e : entries) {
                JsonNode entry = e.path("entry");
                if (entry.path("isFile").asBoolean(false)) {
                    names.add(entry.path("name").asText("unknown"));
                }
            }
        }

        if (names.isEmpty()) {
            return new DocsListNamesResponse(false, "No files found in this service folder", names);
        }
        return new DocsListNamesResponse(true, "OK", names);
    }
}
