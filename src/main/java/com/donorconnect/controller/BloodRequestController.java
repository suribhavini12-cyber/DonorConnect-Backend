package com.donorconnect.controller;

import com.donorconnect.dto.BloodRequestRequest;
import com.donorconnect.dto.BloodRequestResponse;
import com.donorconnect.dto.RequestEventResponse;
import com.donorconnect.dto.StatusUpdateRequest;
import com.donorconnect.security.UserPrincipal;
import com.donorconnect.service.BloodRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/requests")
@Tag(name = "Blood Requests", description = "Blood request lifecycle tracking, status management, and event timeline endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;

    public BloodRequestController(BloodRequestService bloodRequestService) {
        this.bloodRequestService = bloodRequestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('REQUESTER')")
    @Operation(summary = "Create blood request", description = "Creates a new blood request (Requires ROLE_REQUESTER).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blood request created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_REQUESTER")
    })
    public ResponseEntity<BloodRequestResponse> createRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BloodRequestRequest request
    ) {
        BloodRequestResponse response = bloodRequestService.createRequest(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all requests", description = "Retrieves all open or active blood requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of blood requests retrieved")
    })
    public ResponseEntity<List<BloodRequestResponse>> getAllRequests() {
        List<BloodRequestResponse> response = bloodRequestService.getAllRequests();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('REQUESTER')")
    @Operation(summary = "Get my created requests", description = "Retrieves blood requests created by the authenticated requester.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of requester's requests retrieved")
    })
    public ResponseEntity<List<BloodRequestResponse>> getMyRequests(@AuthenticationPrincipal UserPrincipal principal) {
        List<BloodRequestResponse> response = bloodRequestService.getMyRequests(principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get request by ID", description = "Retrieves blood request details by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blood request retrieved"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<BloodRequestResponse> getRequestById(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Blood request ID") @PathVariable Long id
    ) {
        BloodRequestResponse response = bloodRequestService.getRequestById(principal, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('REQUESTER')")
    @Operation(summary = "Update blood request", description = "Updates details of an existing blood request owned by the requester.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blood request updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Not request owner"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<BloodRequestResponse> updateRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Blood request ID") @PathVariable Long id,
            @Valid @RequestBody BloodRequestRequest request
    ) {
        BloodRequestResponse response = bloodRequestService.updateRequest(principal, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('REQUESTER')")
    @Operation(summary = "Cancel blood request", description = "Cancels a blood request owned by the requester.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blood request cancelled"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Not request owner")
    })
    public ResponseEntity<BloodRequestResponse> cancelRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Blood request ID") @PathVariable Long id
    ) {
        BloodRequestResponse response = bloodRequestService.cancelRequest(principal, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('REQUESTER')")
    @Operation(summary = "Update request status", description = "Transitions request status (e.g. FULFILLED, CLOSED, CANCELLED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition")
    })
    public ResponseEntity<BloodRequestResponse> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Blood request ID") @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        BloodRequestResponse response = bloodRequestService.updateRequestStatus(principal, id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get request event timeline", description = "Retrieves historical state events for a blood request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timeline retrieved")
    })
    public ResponseEntity<List<RequestEventResponse>> getRequestTimeline(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Blood request ID") @PathVariable Long id
    ) {
        List<RequestEventResponse> response = bloodRequestService.getRequestTimeline(principal, id);
        return ResponseEntity.ok(response);
    }
}
