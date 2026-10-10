package com.donorconnect.controller;

import com.donorconnect.dto.ReportRequest;
import com.donorconnect.dto.ReportResponse;
import com.donorconnect.security.UserPrincipal;
import com.donorconnect.service.ReportService;
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
@RequestMapping("/api/reports")
@Tag(name = "Reports & Moderation", description = "User reporting endpoints for reporting suspicious users or blood requests")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create moderation report", description = "Submits a report against a user or blood request for admin review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Report created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or missing target"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ReportResponse> createReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportRequest request
    ) {
        ReportResponse response = reportService.createReport(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
