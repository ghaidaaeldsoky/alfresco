package com.companies.alfresco.controller;


import com.companies.alfresco.dto.FilePayloadRequest;
import com.companies.alfresco.dto.FilePayloadResponse;
import com.companies.alfresco.service.AlfrescoUploadService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/test")
public class TestFilePayloadController {

    private final AlfrescoUploadService alfrescoUploadService;

    public TestFilePayloadController(AlfrescoUploadService alfrescoUploadService) {
        this.alfrescoUploadService = alfrescoUploadService;
    }

    // @GetMapping
    // public ResponseEntity<FilePayloadResponse> show(){
    //     return ResponseEntity.ok(
    //                 new FilePayloadResponse("mmmmssggee",true, 29L)
    //         );
    // }

    @PostMapping
    public ResponseEntity<FilePayloadResponse> receiveFile(@RequestBody FilePayloadRequest request) {

        try {
            // Call service to validate + upload to Alfresco
            String alfrescoResponseJson = alfrescoUploadService.uploadBase64File(
                    request.getFileName(),
                    request.getContentBase64(),
                    request.getSize(),
                    request.getParentFolderId(),
                    request.getMimeType()
            );

            
            // Build response for jBPM
            FilePayloadResponse response = new FilePayloadResponse(
                    "Uploaded successfully to Alfresco",
                    true,
                    request.getSize(),
                    alfrescoResponseJson
            );
            // You can add a field for alfrescoResponseJson if you like

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            FilePayloadResponse response = new FilePayloadResponse(
                    "Error: " + ex.getMessage(),
                    false,
                    0,
                    "Error"
            );
            return ResponseEntity.badRequest().body(response);
        }
    //     try {
    //         // Decode Base64 to validate content
    //         byte[] fileBytes = Base64.getDecoder().decode(request.getContentBase64());

    //         System.out.println("File Content: " + fileBytes);
    //         boolean valid = (fileBytes.length == request.getSize());

    //         String msg = valid
    //                 ? "Successfully received file: " + request.getFileName()
    //                 : "Size mismatch! Expected " + request.getSize() + ", got " + fileBytes.length;

    //         return ResponseEntity.ok(
    //                 new FilePayloadResponse(msg, valid, fileBytes.length)
    //         );

    //     } catch (Exception ex) {
    //         return ResponseEntity.badRequest()
    //                 .body(new FilePayloadResponse("Invalid Base64 content", false, 0));
    //     }
    }
}
