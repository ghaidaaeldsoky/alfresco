# 📄 Document Integration Service
This service is a standalone Spring Boot microservice used by jBPM/KIE Server to:
- Upload documents into Alfresco in a deterministic folder hierarchy
- Retrieve documents (all / first)
- List file names under a specific investor/company/service folder

This enables a clean, externalized integration layer without installing custom handlers or dependencies inside jBPM.

> POC scope: **No review UI** and no preview inside the service.
> jBPM can display file names (checklist-like) from the `list-names` endpoint.

---
## 🏗 1. Architecture Overview
All documents are stored under a root folder in Alfresco called:

**`User Documents`** (created under `-root-` if missing)

Inside it, the hierarchy is:

jBPM ➜ REST ➜ Document Integration Service ➜ Alfresco
```bash
User Documents/
INV_{investorId}/
CO_{companyId}/
SRV_{serviceId}/
<uploaded files>
```
Examples:
- `INV_b598367d-bf7d-4556-b07d-a8cf7f0d4ca2`
- `CO_3988c6ca-d818-4f02-ad66-d3f556487cf8`
- `SRV_1dbf15e7-03e3-4e42-8087-61d9d154aa25`

The service does all the integration work, keeping jBPM clean and dependency-free.

---
### ⚙️ 2. Prerequisites

### 2.2 Service configuration
Configure Alfresco connectivity in `application.properties` (or env vars):

```properties
alfresco.base-url=http://localhost:8088
alfresco.username=admin
alfresco.password=admin
```

> The service uses Basic Auth to call Alfresco APIs.

---
## 🔧 3. API Endpoints
Base path: `/api/docs`
### 3.1 Upload a document
**POST** `/api/docs`

Uploads a document to Alfresco under:

`User Documents/INV_{investorId}/CO_{companyId}/SRV_{serviceId}`

**Request:** `DocumentUploadRequest`

jBPM sends a JSON body with the following fields:

```bash
{
  "investorId": "b598367d-bf7d-4556-b07d-a8cf7f0d4ca2",
  "companyId": "3988c6ca-d818-4f02-ad66-d3f556487cf8",
  "serviceId": "1dbf15e7-03e3-4e42-8087-61d9d154aa25",
  "originalFileName": "image.jpg",
  "mimeType": "image/jpeg",
  "size": 94372,
  "contentBase64": "BASE64_ENCODED_BYTES..."
}
```

**Response:** `DocumentUploadResponse`

```bash
{
  "success": true,
  "message": "Uploaded successfully",
  "folderId": "3b075f44-7d49-4981-87ac-e0cb02f2dc2d",
  "nodeId": "0306fcd3-2f3f-4a16-b122-8a9e22df64a5",
  "fileName": "1dbf15e7-03e3-4e42-8087-61d9d154aa25_3988c6ca-d818-4f02-ad66-d3f556487cf8_20251216170739.jpg"
}
```

Notes:

- The service ensures folders exist (creates missing folders).

- The service generates a stable file name (implementation-specific).

---
### 3.2 Retrieve all documents (metadata + base64)

**POST** `/api/docs/retrieve-all`

Returns all documents under:

`User Documents/INV_{investorId}/CO_{companyId}/SRV_{serviceId}`

**Request:** `DocsRetrieveRequest`

```bash
{
  "investorId": "b598367d-bf7d-4556-b07d-a8cf7f0d4ca2",
  "companyId": "3988c6ca-d818-4f02-ad66-d3f556487cf8",
  "serviceId": "1dbf15e7-03e3-4e42-8087-61d9d154aa25"
}
```

**Response:** `DocsRetrieveResponse`

```bash
{
  "success": true,
  "message": "OK",
  "documents": [
    {
      "nodeId": "0306fcd3-2f3f-4a16-b122-8a9e22df64a5",
      "fileName": "....jpg",
      "mimeType": "image/jpeg",
      "size": 94372,
      "contentBase64": "BASE64..."
    }
  ]
}
```

---
### 3.3 Retrieve first document only (best for POC)
**POST** `/api/docs/retrieve-first`

Returns only the  **first file** found under:

`User Documents/INV_{investorId}/CO_{companyId}/SRV_{serviceId}`

**Request:** `DocsRetrieveRequest`

```bash
{
  "investorId": "b598367d-bf7d-4556-b07d-a8cf7f0d4ca2",
  "companyId": "3988c6ca-d818-4f02-ad66-d3f556487cf8",
  "serviceId": "1dbf15e7-03e3-4e42-8087-61d9d154aa25"
}
```

**Response:** `DocFirstResponse`

```bash
{
  "success": true,
  "message": "OK",
  "nodeId": "0306fcd3-2f3f-4a16-b122-8a9e22df64a5",
  "fileName": "....jpg",
  "mimeType": "image/jpeg",
  "size": 94372,
  "contentBase64": "BASE64..."
}
```

---
### 3.4 List file names only (for checklist demo)
**POST** `/api/docs/list-names`

Returns only file names under:

`User Documents/INV_{investorId}/CO_{companyId}/SRV_{serviceId}`

**Request:** `DocsRetrieveRequest`

```bash
{
  "investorId": "b598367d-bf7d-4556-b07d-a8cf7f0d4ca2",
  "companyId": "3988c6ca-d818-4f02-ad66-d3f556487cf8",
  "serviceId": "1dbf15e7-03e3-4e42-8087-61d9d154aa25"
}
```

**Response:** `DocsListNamesResponse`

```bash
{
  "success": true,
  "message": "OK",
  "fileNames": [
    "file1.jpg",
    "file2.jpg"
  ]
}
```

---
## 🗒 4. jBPM Notes (How to Call These APIs)
### 4.1 Upload flow (jBPM side)

Typical variables:

- `document` : `org.jbpm.document.Document`

- `DocumentPayload` : `Object` (Map)

- `DocumentResponse` : `String` (REST result)

- `username` : `String` (optional, if used)

Typical steps:

1. Human Task: user uploads `document`
2. Script Task: build payload JSON:
fileName, size, mimeType, contentBase64
investorId, companyId, serviceId
3. REST Task: call `POST /api/docs` with JSON body
4. Use returned `nodeId` (optional) for logging/demo

### 4.2 Retrieve/list flow (jBPM side)
For demo without preview:

1. Script Task: build JSON `{investorId, companyId, serviceId}`
2. REST Task: call `POST /api/docs/list-names`
3. Script Task: convert response to a text checklist and show it in a Human Task
