# 📄 Document Integration Service
This service is a standalone Spring Boot microservice used by jBPM/KIE Server to:
- Upload documents into Alfresco in a deterministic folder hierarchy
- Retrieve documents (all / first)
- List file names under a specific investor/company/service folder
- Retrieve document compliance status
- Update document compliance status (single & bulk)

This enables a clean, externalized integration layer without installing custom handlers or dependencies inside jBPM.

> POC scope:
>   - **No review UI** and no preview inside the service. <br>
>   - jBPM displays document names and drives approval decisions.

---
## 🏗 1. Architecture Overview
All documents are stored under a root folder in Alfresco called:

**`User Documents`** (created under `-root-` if missing)

Inside it, the hierarchy is:

jBPM ➜ REST ➜ Document Integration Service ➜ Alfresco
```bash
User Documents/
 └─ INV_{investorId}/
    └─ CO_{companyId}/
       └─ SRV_{serviceId}/
          └─ <uploaded files>
```
Examples:
- `INV_b598367d-bf7d-4556-b07d-a8cf7f0d4ca2`
- `CO_3988c6ca-d818-4f02-ad66-d3f556487cf8`
- `SRV_1dbf15e7-03e3-4e42-8087-61d9d154aa25`

The service does all the integration work, keeping jBPM clean and dependency-free.

---
## ⚙️ 2. Prerequisites

### 2.1 Alfresco – Content Model & Aspects (MANDATORY)

Before using the service, **Alfresco must be configured** with a custom model and aspects.

---

### 2.1.1 Create Content Model (Model Manager)

Use **Alfresco Model Manager** UI.

**Model**

* Name: `Document`
* Namespace URI:
  `http://www.companies.com/model/docs/1.0`
* Prefix: `cp`

📺 Video reference (official):
[https://docs.alfresco.com/content-services/7.1/tutorial/video/model/](https://docs.alfresco.com/content-services/7.1/tutorial/video/model/)

---

### 2.1.2 Create Aspect: `cp:complianceInfo`

Add a new **Aspect** under the model:

**Aspect name**

```
cp:complianceInfo
```

**Properties**

| Property                | Type | Allowed Values                    |
| ----------------------- | ---- | --------------------------------- |
| `cp:approvalStatus`     | List | `PENDING`, `APPROVED`, `REJECTED` |
| `cp:verificationStatus` | List | `UNVERIFIED`, `VERIFIED`          |
| `cp:reviewReason`       | Text | Free text                         |

---

### 2.1.3 Apply Aspect via Folder Rule (Default Values)

The **root folder** `User Documents` must automatically apply default values.

#### Steps in Alfresco Share:

1. Go to **User Documents** folder
2. Manage Rules
3. Create Rule

**Rule configuration**

* **Name**: `default-status`
* **Description**: Set default compliance status
* ✅ Active
* ✅ Run in background
* ✅ Rule applies to subfolders

**When**

* Items are created or enter this folder

**If all criteria are met**

* Is of type (or sub-type): `Content`

**Perform Action**

* Set property value:

  * `cp:approvalStatus = PENDING`
  * `cp:verificationStatus = UNVERIFIED`

This ensures **every uploaded file starts with default compliance values**.

---

### 2.2 Service Configuration

`application.properties`

```properties
alfresco.base-url=http://localhost:8088
alfresco.username=admin
alfresco.password=admin
```

The service uses **Basic Auth** for Alfresco APIs.

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

### 3.5 Retrieve Documents Status (NEW)

**POST** `/api/docs/retrieve-status`

Returns **document metadata + compliance status**.

**Request – `DocsRetrieveRequest`**

```json
{
  "investorId": "INV_ID",
  "companyId": "COMP_ID",
  "serviceId": "SRV_ID"
}
```

**Response**

```json
{
  "success": true,
  "message": "OK",
  "documents": [
    {
      "fileName": "Passport.pdf",
      "nodeId": "NODE_ID",
      "approvalStatus": "APPROVED",
      "verificationStatus": "VERIFIED",
      "isFinal": false,
      "reason": ""
    }
  ]
}
```

Used by jBPM to **drive gateways and decisions**.

---

### 3.6 Update Status of Multiple Documents (NEW)

**POST** `/api/docs/update-status-bulk`

Updates compliance status for multiple files in one call.

**Request**

```json
{
  "investorId": "INV_ID",
  "companyId": "COMP_ID",
  "serviceId": "SRV_ID",
  "documents": [
    {
      "fileName": "Passport.pdf",
      "approvalStatus": "APPROVED",
      "verificationStatus": "VERIFIED",
      "reason": "Approved by manager"
    },
    {
      "fileName": "BankStatement.pdf",
      "approvalStatus": "REJECTED",
      "verificationStatus": "UNVERIFIED",
      "reason": "Image not clear"
    }
  ]
}
```

**Response**

```json
{
  "success": true,
  "message": "Statuses updated successfully"
}
```

---

## 🗒 4. jBPM Integration Notes

### 4.1 Retrieve Status Flow (Recommended)

**Variables**

* `RetrieveStatusReq` : Object
* `RetrieveStatusRes` : Object
* `documents` : Object
* `allApproved` : Boolean
* `allVerified` : Boolean
* `needOriginals` : Boolean
* `readyToPrint` : Boolean

**Flow**

1. Script Task → build request Map
2. REST Task → `/api/docs/retrieve-status`
3. Script Task → parse Map + compute flags
4. XOR Gateway → route based on flags

---

### 4.2 Update Status Flow (Manager Decision)

**Variables**

* `fileNames` : List<String>
* `manager_approve` : Boolean
* `manager_verified` : Boolean
* `bulkUpdatePayload` : Object

**Flow**

1. Human Task → manager decision
2. Script Task → build bulk update Map
3. REST Task → `/api/docs/update-status-bulk`
