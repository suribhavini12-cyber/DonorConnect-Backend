package com.donorconnect.controller;

import com.donorconnect.dto.*;
import com.donorconnect.security.UserPrincipal;
import com.donorconnect.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Admin-only dashboard metrics, user activation/deactivation, request moderation, and report resolution (Requires ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard metrics", description = "Retrieves high-level platform statistics (users, requests, reports breakdown).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard stats retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_ADMIN")
    })
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        AdminDashboardResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports")
    @Operation(summary = "Get all moderation reports", description = "Retrieves all user/request moderation reports.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports list retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_ADMIN")
    })
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<ReportResponse> response = adminService.getAllReports();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reports/{id}")
    @Operation(summary = "Update report status", description = "Updates status of a moderation report (OPEN, UNDER_REVIEW, RESOLVED, DISMISSED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report status updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<ReportResponse> updateReportStatus(
            @Parameter(description = "Report ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportStatusUpdateRequest request
    ) {
        ReportResponse response = adminService.updateReportStatus(id, principal, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all platform users", description = "Retrieves user accounts list for admin moderation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users list retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_ADMIN")
    })
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<AdminUserResponse> response = adminService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate user", description = "Toggles user active status. Prevents deactivating sole active admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated"),
            @ApiResponse(responseCode = "400", description = "Cannot deactivate sole active admin"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_ADMIN")
    })
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        AdminUserResponse response = adminService.updateUserStatus(id, principal, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests")
    @Operation(summary = "Get all requests for moderation", description = "Retrieves all blood requests for admin moderation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requests list retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_ADMIN")
    })
    public ResponseEntity<List<AdminRequestResponse>> getAllRequests() {
        List<AdminRequestResponse> response = adminService.getAllRequests();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/requests/{id}/status")
    @Operation(summary = "Moderate request status", description = "Admin status override for any blood request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request status updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Requires ROLE_ADMIN")
    })
    public ResponseEntity<AdminRequestResponse> updateRequestStatus(
            @Parameter(description = "Request ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdminRequestStatusUpdateRequest request
    ) {
        AdminRequestResponse response = adminService.updateRequestStatus(id, principal, request);
        return ResponseEntity.ok(response);
    }
}
