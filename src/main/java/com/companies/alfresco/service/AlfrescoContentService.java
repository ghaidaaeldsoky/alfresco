package com.companies.alfresco.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AlfrescoContentService {

    private final RestTemplate restTemplate;
    private final AlfrescoAuthService auth;
    private final String alfrescoBaseUrl;

    public AlfrescoContentService(RestTemplate restTemplate,
                                  AlfrescoAuthService auth,
                                  @Value("${alfresco.base-url}") String alfrescoBaseUrl) {
        this.restTemplate = restTemplate;
        this.auth = auth;
        this.alfrescoBaseUrl = alfrescoBaseUrl;
    }

    public ResponseEntity<byte[]> getNodeContent(String nodeId, boolean attachment) {
        String url = alfrescoBaseUrl
                + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
                + nodeId + "/content?attachment=" + attachment;

        HttpHeaders headers = auth.authHeaders();

        ResponseEntity<byte[]> resp = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

        // forward content-type + content-disposition from Alfresco as-is
        HttpHeaders out = new HttpHeaders();
        out.putAll(resp.getHeaders());

        return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
    }
}
