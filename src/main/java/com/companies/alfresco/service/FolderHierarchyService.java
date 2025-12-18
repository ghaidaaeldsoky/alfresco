package com.companies.alfresco.service;

import org.springframework.stereotype.Service;

@Service
public class FolderHierarchyService {

    private final AlfrescoNodeService nodeService;

    public FolderHierarchyService(AlfrescoNodeService nodeService) {
        this.nodeService = nodeService;
    }

    public String ensureHierarchy(String investorId, String companyId, String serviceId) {

        String root = nodeService.getOrCreateUserDocumentsRoot();

        String invFolder = nodeService.createOrGetFolder(root, "INV_" + investorId);
        String compFolder = nodeService.createOrGetFolder(invFolder, "CO_" + companyId);
        String svcFolder = nodeService.createOrGetFolder(compFolder, "SRV_" + serviceId);

        return svcFolder; // final folderId where docs should be uploaded
    }

    public String buildRelativePath(String investorId, String companyId, String serviceId) {
        return "User Documents/INV_" + investorId + "/CO_" + companyId + "/SRV_" + serviceId;
    }
}
