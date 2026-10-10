package com.donorconnect.controller;

import com.donorconnect.dto.DonorResponseRequest;
import com.donorconnect.dto.DonorResponseResponse;
import com.donorconnect.dto.ResponseStatusUpdateRequest;
import com.donorconnect.security.UserPrincipal;
import com.donorconnect.service.DonorResponseService;
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
@Tag(name = "Donor Responses", description = "Donor interest expressions, response lifecycle tracking, and status updates (INTERESTED, CONTACTED, CONFIRMED, WITHDRAWN)")
@SecurityRequirement(name = "bearerAuth")
public class DonorResponseController {

    private final DonorResponseService donorResponseService;

    public DonorResponseController(DonorResponseService donorResponseService) {
        this.donorResponseService = donorResponseService;
    }

    @PostMapping("/api/requests/{requestId}/responses")
    @PreAuthorize("hasRole('DONOR')")
    @Operation(summary = "Express interest in blood request", description = "Donor expresses interest in helping with a potential blood request (Requires ROLE_DONOR).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Interest expressed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or request is closed/cancelled"),
            @ApiResponse(responseCode = "409", description = "Active response already exists for this donor")
    })
    public ResponseEntity<DonorResponseResponse> createResponse(
            @Parameter(description = "Blood request ID") @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody(required = false) DonorResponseRequest request
    ) {
        if (request == null) {
            request = new DonorResponseRequest();
        }
        DonorResponseResponse response = donorResponseService.createResponse(requestId, principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/requests/{requestId}/responses")
    @PreAuthorize("hasRole('REQUESTER')")
    @Operation(summary = "Get responses for blood request", description = "Requester views donor responses received for their blood request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of donor responses retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Not request owner")
    })
    public ResponseEntity<List<DonorResponseResponse>> getResponsesForRequest(
            @Parameter(description = "Blood request ID") @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DonorResponseResponse> response = donorResponseService.getResponsesForRequest(requestId, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/responses/my")
    @PreAuthorize("hasRole('DONOR')")
    @Operation(summary = "Get my donor responses", description = "Donor views interest responses they have submitted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of donor's responses retrieved")
    })
    public ResponseEntity<List<DonorResponseResponse>> getMyResponses(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DonorResponseResponse> response = donorResponseService.getMyResponses(principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/responses/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get response by ID", description = "Retrieves specific response details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response details retrieved"),
            @ApiResponse(responseCode = "404", description = "Response not found")
    })
    public ResponseEntity<DonorResponseResponse> getResponseById(
            @Parameter(description = "Response ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DonorResponseResponse response = donorResponseService.getResponseById(id, principal);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/responses/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update response status", description = "Updates response status (Donor: WITHDRAWN; Requester: CONTACTED, CONFIRMED_BY_REQUESTER).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition")
    })
    public ResponseEntity<DonorResponseResponse> updateResponseStatus(
            @Parameter(description = "Response ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResponseStatusUpdateRequest request
    ) {
        DonorResponseResponse response = donorResponseService.updateResponseStatus(id, principal, request);
        return ResponseEntity.ok(response);
    }
}
