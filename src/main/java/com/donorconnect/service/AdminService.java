package com.donorconnect.service;

import com.donorconnect.dto.*;
import com.donorconnect.exception.InvalidStatusTransitionException;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.*;
import com.donorconnect.repository.*;
import com.donorconnect.security.UserPrincipal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final RequestEventRepository requestEventRepository;
    private final ReportService reportService;

    public AdminService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            BloodRequestRepository bloodRequestRepository,
            RequestEventRepository requestEventRepository,
            ReportService reportService
    ) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.requestEventRepository = requestEventRepository;
        this.reportService = reportService;
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(reportService::mapToReportResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportResponse updateReportStatus(Long reportId, UserPrincipal principal, ReportStatusUpdateRequest requestDto) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found with ID: " + reportId));

        report.setStatus(requestDto.getStatus());
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedBy(principal.getUser());

        Report savedReport = reportRepository.save(report);
        return reportService.mapToReportResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "openRequests", allEntries = true),
            @CacheEvict(value = "donorMatches", allEntries = true)
    })
    public AdminUserResponse updateUserStatus(Long userId, UserPrincipal principal, AdminUserStatusUpdateRequest requestDto) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        if (targetUser.getRole() == Role.ADMIN && Boolean.FALSE.equals(requestDto.getActive())) {
            long activeAdminCount = userRepository.countByRoleAndActiveTrue(Role.ADMIN);
            if (activeAdminCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot deactivate the only active admin account.");
            }
        }

        targetUser.setActive(requestDto.getActive());
        User savedUser = userRepository.save(targetUser);
        return mapToAdminUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<AdminRequestResponse> getAllRequests() {
        return bloodRequestRepository.findAll().stream()
                .map(this::mapToAdminRequestResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "openRequests", allEntries = true),
            @CacheEvict(value = "donorMatches", allEntries = true)
    })
    public AdminRequestResponse updateRequestStatus(Long requestId, UserPrincipal principal, AdminRequestStatusUpdateRequest requestDto) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + requestId));

        RequestStatus currentStatus = bloodRequest.getStatus();
        RequestStatus newStatus = requestDto.getStatus();

        if (currentStatus == RequestStatus.CANCELLED || currentStatus == RequestStatus.CLOSED || currentStatus == RequestStatus.FULFILLED) {
            if (newStatus == RequestStatus.PENDING_REVIEW) {
                throw new InvalidStatusTransitionException("Cannot revert terminal request status " + currentStatus + " back to PENDING_REVIEW");
            }
        }

        bloodRequest.setStatus(newStatus);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        RequestEvent event = new RequestEvent();
        event.setRequest(savedRequest);
        event.setActor(principal.getUser());
        event.setEventType("STATUS_CHANGED");
        event.setDescription("Blood request status changed to " + newStatus + " by admin");
        requestEventRepository.save(event);

        return mapToAdminRequestResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long donorCount = userRepository.countByRole(Role.DONOR);
        long requesterCount = userRepository.countByRole(Role.REQUESTER);
        long adminCount = userRepository.countByRole(Role.ADMIN);

        long totalRequests = bloodRequestRepository.count();
        long openRequests = bloodRequestRepository.countByStatus(RequestStatus.OPEN);
        long fulfilledRequests = bloodRequestRepository.countByStatus(RequestStatus.FULFILLED);
        long cancelledRequests = bloodRequestRepository.countByStatus(RequestStatus.CANCELLED);

        long totalReports = reportRepository.count();
        long openReports = reportRepository.countByStatus(ReportStatus.OPEN);
        long resolvedReports = reportRepository.countByStatus(ReportStatus.RESOLVED);

        return new AdminDashboardResponse(
                totalUsers,
                activeUsers,
                donorCount,
                requesterCount,
                adminCount,
                totalRequests,
                openRequests,
                fulfilledRequests,
                cancelledRequests,
                totalReports,
                openReports,
                resolvedReports
        );
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private AdminRequestResponse mapToAdminRequestResponse(BloodRequest request) {
        return new AdminRequestResponse(
                request.getId(),
                request.getRequester().getId(),
                request.getRequester().getName(),
                request.getBloodGroup(),
                request.getBloodComponent(),
                request.getUnitsRequired(),
                request.getHospitalName(),
                request.getHospitalLocation(),
                request.getUrgency(),
                request.getRequiredBy(),
                request.getDescription(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
