# DonorConnect Platform Guide and Request Tracking

Title: DonorConnect Platform Guide and Request Tracking
Topic: Platform Guide
Source: DonorConnect Knowledge Base

## Overview of DonorConnect Platform

DonorConnect is a blood donor discovery and coordination platform that connects blood requesters with potential blood donors. The platform allows registered users to create blood requests, track request statuses, find potential donor matches, submit donor responses, and communicate status updates through a structured event timeline.

---

## Creating and Tracking Blood Requests for Requesters

### How Requesters Create a Blood Request
Users registered with the `REQUESTER` role can create a new blood request by navigating to the Create Request section (`/requests/new`). 
When creating a request, the requester provides:
- Required Blood Group (e.g., A+, O-, B+, AB+)
- Blood Component (e.g., Whole Blood, Red Blood Cells, Platelets, Plasma)
- Number of Units Required
- Hospital Name and Hospital Location
- Urgency Level (LOW, MEDIUM, HIGH, CRITICAL)
- Required By Date/Time
- Additional Description or Notes

Upon creation, the new request is assigned an initial status of `PENDING_REVIEW`.

### How Requesters Track the Status of Their Blood Request
Requesters can track and monitor their blood requests at any time by following these steps:
1. Navigate to the **My Requests** page (`/requests/my` or `/requests`).
2. View the list of all created requests, which displays the current status (such as `PENDING_REVIEW`, `OPEN`, `PARTIALLY_RESPONDED`, `FULFILLED`, `CANCELLED`, or `CLOSED`).
3. Click on a specific blood request to view detailed information (`/requests/:id`).
4. On the request details view, requesters can inspect:
   - **Request Event Timeline**: A chronological history of status transitions and events associated with the request (`GET /api/requests/{id}/timeline`).
   - **Potential Donor Matches**: A list of active donors matching the blood group and location criteria (`GET /api/requests/{id}/matches`).
   - **Received Donor Responses**: Expressions of interest submitted by potential donors (`GET /api/requests/{id}/responses`).

---

## Understanding Blood Request Statuses

Each blood request on DonorConnect moves through a defined lifecycle represented by the following request statuses:

1. **PENDING_REVIEW**:
   - The initial status assigned when a requester submits a new blood request.
   - The request is pending administrative review or validation before becoming publicly active on the platform.

2. **OPEN**:
   - The blood request has been approved or activated and is publicly visible to registered donors.
   - Donors can view open requests and express interest in donating.

3. **PARTIALLY_RESPONDED**:
   - One or more donors have submitted an expression of interest (`INTERESTED`) for the blood request, but the requested units are not yet fully confirmed or satisfied.

4. **FULFILLED**:
   - The blood request has been successfully met and all required units have been satisfied.
   - The requester or administrator marks the request as `FULFILLED`.

5. **CANCELLED**:
   - The blood request was cancelled by the requester or an administrator prior to fulfillment.
   - No further donor responses can be submitted for a cancelled request.

6. **CLOSED**:
   - The blood request has reached its end of lifecycle, expired, or been closed by an administrator or requester.

---

## Donor Workflow: Finding Requests and Expressing Interest

### How Donors Find Blood Requests
Users registered with the `DONOR` role can browse active blood requests by navigating to the **Available Requests** page (`/donor/requests`). 
Donors can view requests that match their blood group, location, or urgency requirements.

### How Donors Express Interest in a Request
When a donor views the details of an open blood request (`/donor/requests/:id`), they can click **Express Interest** (`POST /api/requests/{requestId}/responses`). 
Expressing interest submits a response to the requester, signifying that the donor is willing to assist.

### Understanding Donor Response Statuses
Each donor response follows a tracking lifecycle:
- **INTERESTED**: Initial state when a donor expresses interest in helping with a blood request.
- **CONTACTED**: The requester has reviewed the donor's interest and initiated contact with the donor.
- **CONFIRMED_BY_REQUESTER**: The requester has confirmed that the donor will assist with the blood donation.
- **WITHDRAWN**: The donor has withdrawn their expression of interest (`PATCH /api/responses/{id}`).

Donors can view all their submitted responses on the **My Responses** page (`/donor/responses`).

---

## Reviewing Donor Responses and Potential Matches

### Reviewing Donor Responses
Requesters can view all responses received for their blood request on the request details page. Requesters can update a response status to `CONTACTED` or `CONFIRMED_BY_REQUESTER` as coordination progresses.

### Potential Donor Matches
Requesters can click **Find Potential Matches** (`/requests/:id/matches`) to retrieve a list of potential donor matches. 
- Matching is based on exact blood group compatibility, hospital location, active donor profile status, and donor consent.
- **Important Safety Disclaimer**: Matching on DonorConnect represents potential coordination fit only. It does NOT determine medical eligibility, clinical compatibility, or guaranteed donor availability. Medical testing and final donation decisions belong exclusively to qualified healthcare professionals and authorized blood banks.

---

## Fulfilling or Cancelling a Request

### Fulfilling a Request
When the required blood units are secured, the requester updates the status of the blood request to `FULFILLED` via the request status update option (`PATCH /api/requests/{id}/status`).

### Cancelling a Request
If a blood request is no longer needed, the requester can cancel the request (`DELETE /api/requests/{id}` or `PATCH /api/requests/{id}/status`), which updates its status to `CANCELLED`.

### Event Timeline Recording
Every status change (e.g., transition from `OPEN` to `PARTIALLY_RESPONDED`, `FULFILLED`, or `CANCELLED`) automatically creates a `RequestEvent` entry recorded in the request timeline, ensuring full auditability and transparency for the requester.

---

## Frequently Asked Questions (FAQ)

### Q: How can I track the status of my blood request in DonorConnect?
**A:** As a requester, you can track the status of your blood request by logging into your account and navigating to the **My Requests** page (`/requests/my`). Click on any request to view its current status (such as `PENDING_REVIEW`, `OPEN`, `PARTIALLY_RESPONDED`, `FULFILLED`, or `CANCELLED`), view received donor responses, check potential donor matches, and inspect the chronological event timeline (`/api/requests/{id}/timeline`).

### Q: Where can I see my existing blood requests and status updates?
**A:** All your created requests and their status updates are listed under **My Requests** (`/requests/my`). Opening a request details page shows all real-time status updates and event timeline entries.

### Q: What do the different request statuses mean?
**A:** 
- `PENDING_REVIEW`: Awaiting review or approval before public listing.
- `OPEN`: Active request visible to potential donors.
- `PARTIALLY_RESPONDED`: Donors have expressed interest in helping.
- `FULFILLED`: Required blood units have been fully satisfied.
- `CANCELLED`: Request was cancelled by the requester or admin.
- `CLOSED`: Request lifecycle is finished or closed.

### Q: How do donors find and respond to blood requests?
**A:** Donors navigate to **Available Requests** (`/donor/requests`), view open blood requests matching their blood group, and click **Express Interest** to submit a response with status `INTERESTED`.

### Q: How can requesters review donor responses and potential matches?
**A:** Requesters open their request details page to view submitted donor responses (`/api/requests/{id}/responses`) and click **Potential Matches** (`/requests/:id/matches`) to discover compatible registered donors.

### Q: What happens when a request is fulfilled or cancelled?
**A:** When marked as `FULFILLED` or `CANCELLED`, the request status is updated, a status transition event is appended to the request timeline (`GET /api/requests/{id}/timeline`), and the request is closed to further responses.

**Q: What should I do if no donors respond to my blood request?**

**A:** If no donors have responded to your blood request, open **My Requests** (`/requests/my`) and select your request to check its current status. Review the **Potential Donor Matches** and **Received Donor Responses** sections to see whether any potential matches or responses are available. If your request is still open and you need further assistance, contact the appropriate DonorConnect administrator or healthcare provider for guidance. Donor matches do not guarantee donor availability or clinical compatibility.
