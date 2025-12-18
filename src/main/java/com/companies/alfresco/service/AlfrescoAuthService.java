package com.companies.alfresco.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class AlfrescoAuthService {

    @Value("${alfresco.username}")
    private String alfrescoUsername;

    @Value("${alfresco.password}")
    private String alfrescoPassword;

    public HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = alfrescoUsername + ":" + alfrescoPassword;
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }


    
}
