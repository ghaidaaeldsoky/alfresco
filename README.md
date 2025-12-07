## 📄 Document Integration Service
This service is a standalone Spring Boot microservice used by jBPM/KIE Server to:
1. Receive document metadata and Base64 content from jBPM
2. Ensure the user has a dedicated folder in Alfresco (`cm:folder`)
3. Upload the document into that folder
4. Return the Alfresco nodeId and JSON metadata back to jBPM
5.Allow jBPM to reference/view later documents

This enables a clean, externalized integration layer without installing custom handlers or dependencies inside jBPM.

---
### 🏗 1. Architecture Overview
jBPM ➜ REST ➜ Document Integration Service ➜ Alfresco
```bash
User Uploads Document
        │
        ▼
jBPM Human Task ("Upload Document")
        │
        │ produces:
        │   - document (org.jbpm.document.Document)
        │
        ▼
Script Task (build payload)
        │
        ▼
REST Task → POST /test
        │
        ▼
Document Integration Service
  • Decode Base64
  • Ensure user folder in Alfresco exists
  • Upload document (multipart)
  • Return nodeId + JSON
```
The service does all the integration work, keeping jBPM clean and dependency-free.

---
### ⚙️ 2. Service Responsibilities
✔ Receive structured JSON from jBPM

✔ Decode Base64 to a binary file

✔ Validate size matches original

✔ Ensure user folder exists in Alfresco (“UserDocuments/{username}”)

✔ Upload file to Alfresco using multipart/form-data

✔ Return Alfresco metadata including `nodeId`

---
### 🔧 3. Request Format (from jBPM)
jBPM sends a JSON body with the following fields:
```bash
{
  "fileName": "document.pdf",
  "contentBase64": "JVBERi0xLjQKJcfs...",
  "size": 102304,
  "username": "ghaidaa",
  "parentFolderId": null,
  "mimeType": "application/pdf"
}
```
**Request Fields**
| Field            | Type   | Required | Description                                                                 |
| ---------------- | ------ | -------- | --------------------------------------------------------------------------- |
| `fileName`       | String | ✔️ Yes   | Original document name from jBPM                                            |
| `contentBase64`  | String | ✔️ Yes   | Base64 content of the file                                                  |
| `size`           | long   | ✔️ Yes   | Original file size (bytes)                                                  |
| `username`       | String | ✔️ Yes   | Used to determine folder path in Alfresco                                   |
| `parentFolderId` | String | Optional | If supplied, upload document in this folder instead of creating user folder |
| `mimeType`       | String | Optional | Defaults to `application/octet-stream`                                      |

---
### 📤 4. Response Format (to jBPM)
Upon success, the service returns:
```bash
{
  "message": "Uploaded to user folder successfully",
  "success": true,
  "receivedSize": 102304,
  "nodeId": "d3f1cfae-3fc8-4e32-b4cb-dfa10f4eac21",
  "fileName": "document.pdf",
  "alfrescoResponseJson": "{...full JSON from Alfresco...}"
}
```
| Field                  | Type    | Description                                          |
| ---------------------- | ------- | ---------------------------------------------------- |
| `message`              | String  | Human-readable upload status                         |
| `success`              | boolean | Upload succeeded or failed                           |
| `receivedSize`         | long    | File size decoded from Base64                        |
| `nodeId`               | String  | The Alfresco node id for the uploaded file           |
| `fileName`             | String  | Name of uploaded file                                |
| `alfrescoResponseJson` | String  | Full Alfresco JSON response for debugging or storage |

This `nodeId` is used by jBPM for retrieval & viewing later.

---
### 🗂 5. Alfresco Folder Structure Logic
#### Root configuration (in application.properties):
```bash
alfresco.users-root-id=<nodeId of UserDocuments folder>
```
#### Folder creation flow:
1. Check if folder /UserDocuments/{username} exists

2. If exists → use its nodeId

3. If not → create a new folder:
```bash
{
  "name": "{username}",
  "nodeType": "cm:folder"
}
```
4. Upload document inside this folder
`/nodes/{userFolderId}/children`

#### Upload endpoint (multipart):
```bash
POST /alfresco/api/-default-/public/alfresco/versions/1/nodes/{folderId}/children
```
Multipart fields:

- `filedata` → binary data

- `name` → fileName

- `nodeType` → "cm:content"

---
### 📦 6. Microservice Components
**`1. UserFolderService`**

- Finds or creates a folder for the user in Alfresco

- Returns the folder nodeId

**`2. AlfrescoUploadService`**

- Decodes Base64 → bytes

- Builds multipart request

- Uploads to Alfresco

- Returns raw Alfresco JSON

***`3. TestFilePayloadController`**

- Receives JSON request

- Calls folder service → gets folderId

- Calls upload service → gets Alfresco JSON

- Extracts nodeId and returns structured response to jBPM

---
### 🔄 7. jBPM Required Variables
Inside your jBPM process, you must define these variables:
| Variable Name      | Type                         | Purpose                        |
| ------------------ | ---------------------------- | ------------------------------ |
| `document`         | `org.jbpm.document.Document` | File uploaded by human task    |
| `DocumentPayload`  | `java.util.Map` or `Object`  | JSON payload sent to REST task |
| `DocumentResponse` | `Object` or `String`         | REST reply from microservice   |
| `username`         | `String`                     | Alfresco folder owner          |

---
### 🧩 8. jBPM Script Task — Build Payload
```bash
java.util.Map payload = new java.util.HashMap();
payload.put("fileName", document.getName());
payload.put("contentBase64",
    java.util.Base64.getEncoder().encodeToString(document.getContent()));
payload.put("size", document.getSize());
payload.put("username", username);
// payload.put("mimeType", "application/pdf");  // for Example

kcontext.setVariable("DocumentPayload", payload);
```
---
### 🌐 9. jBPM REST Work Item Configuration

#### Method
```bash
POST
```
#### URL
```bash
http://your-service-host:8185/test
```
#### Content Type
```bash
application/json
```
#### Input Mapping
```bash
ContentData → DocumentPayload
```
#### Output Mapping
```bash
Result → DocumentResponse
```
---
### 🔍 10. Accessing the returned Alfresco nodeId in jBPM
If you map `Result` → `DocumentResponse` (String):
Use a Script Task:
```bash
String resp = (String) kcontext.getVariable("DocumentResponse");
String nodeId = null;

int idx = resp.indexOf("\"nodeId\":\"");
if (idx != -1) {
    int start = idx + "\"nodeId\":\"".length();
    int end = resp.indexOf("\"", start);
    nodeId = resp.substring(start, end);
}

kcontext.setVariable("alfrescoNodeId", nodeId);
```

Now you can:

✔ Display it to user

✔ Use it in next Alfresco call

✔ Generate a "Download" or "Preview" link

---
### 👁️ 11. Previewing or Downloading Files from jBPM
Alfresco document content URL:
```bash
GET http://<alfresco-host>/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content
```
You may inject this URL into a form or send it to a UI.

---
### 🚀 12. Summary
This microservice successfully externalizes document handling:
| jBPM handles          | Service handles              |
| --------------------- | ---------------------------- |
| Human Task – Get file | Base64 decoding              |
| Build JSON payload    | Alfresco folder check/create |
| REST call             | Multipart upload             |
| Store nodeId          | Return structured response   |

This architecture keeps jBPM **clean, portable, and free of binary-processing dependencies**.
