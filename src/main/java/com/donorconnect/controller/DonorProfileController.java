package com.donorconnect.controller;

import com.donorconnect.dto.DonorProfileRequest;
import com.donorconnect.dto.DonorProfileResponse;
import com.donorconnect.dto.ParticipationUpdateRequest;
import com.donorconnect.security.UserPrincipal;
import com.donorconnect.service.DonorProfileService;
import io.swagger.v3.oas.annotations.Operation;
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

@CrossOrigin
@RestController
@RequestMapping("/api/donors")
@Tag(name = "Donor Profiles", description = "Donor profile creation, retrieval, updates, and participation status management (Requires ROLE_DONOR)")
@SecurityRequirement(name = "bearerAuth")
public class DonorProfileController {

    private final DonorProfileService donorProfileService;

    public DonorProfileController(DonorProfileService donorProfileService) {
        this.donorProfileService = donorProfileService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    @Operation(summary = "Get donor profile", description = "Retrieves the authenticated donor's profile details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_DONOR"),
            @ApiResponse(responseCode = "404", description = "Donor profile does not exist yet")
    })
    public ResponseEntity<DonorProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        DonorProfileResponse response = donorProfileService.getProfile(principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    @Operation(summary = "Create donor profile", description = "Creates a new donor profile for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Donor profile already exists")
    })
    public ResponseEntity<DonorProfileResponse> createProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DonorProfileRequest request
    ) {
        DonorProfileResponse response = donorProfileService.createProfile(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    @Operation(summary = "Update donor profile", description = "Updates blood group, location, and consent settings for the authenticated donor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<DonorProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DonorProfileRequest request
    ) {
        DonorProfileResponse response = donorProfileService.updateProfile(principal, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/participation")
    @PreAuthorize("hasRole('DONOR')")
    @Operation(summary = "Update donor participation status", description = "Updates participation status to ACTIVE or PAUSED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participation status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status requested")
    })
    public ResponseEntity<DonorProfileResponse> updateParticipation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ParticipationUpdateRequest request
    ) {
        DonorProfileResponse response = donorProfileService.updateParticipationStatus(
                principal,
                request.getParticipationStatus()
        );
        return ResponseEntity.ok(response);
    }
}
