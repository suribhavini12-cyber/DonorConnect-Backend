package com.donorconnect.controller;

import com.donorconnect.dto.DonorMatchResponse;
import com.donorconnect.security.UserPrincipal;
import com.donorconnect.service.DonorMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/requests")
@Tag(name = "Donor Matching", description = "Discovery endpoints for potential donor matches (Requires ROLE_REQUESTER)")
@SecurityRequirement(name = "bearerAuth")
public class DonorMatchingController {

    private final DonorMatchingService donorMatchingService;

    public DonorMatchingController(DonorMatchingService donorMatchingService) {
        this.donorMatchingService = donorMatchingService;
    }

    @GetMapping("/{id}/matches")
    @PreAuthorize("hasRole('REQUESTER')")
    @Operation(
            summary = "Get potential donor matches",
            description = "Finds potential donor matches for a specific blood request based on exact blood group, location, active participation, and consent. NOTE: Represents coordination matches only, NOT medical eligibility or clinical approval."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Potential donor matches retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requester does not own this request or user lacks ROLE_REQUESTER"),
            @ApiResponse(responseCode = "404", description = "Blood request not found")
    })
    public ResponseEntity<List<DonorMatchResponse>> getPotentialMatches(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Blood request ID") @PathVariable Long id
    ) {
        List<DonorMatchResponse> matches = donorMatchingService.findPotentialMatches(principal, id);
        return ResponseEntity.ok(matches);
    }
}
