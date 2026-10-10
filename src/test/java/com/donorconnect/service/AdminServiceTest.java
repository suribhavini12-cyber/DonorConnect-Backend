package com.donorconnect.service;

import com.donorconnect.dto.*;
import com.donorconnect.model.*;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.ReportRepository;
import com.donorconnect.repository.RequestEventRepository;
import com.donorconnect.repository.UserRepository;
import com.donorconnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private RequestEventRepository requestEventRepository;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private UserPrincipal adminPrincipal;
    private User targetUser;
    private Report mockReport;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("System Admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
        adminPrincipal = new UserPrincipal(adminUser);

        targetUser = new User();
        targetUser.setId(60L);
        targetUser.setName("Target User");
        targetUser.setRole(Role.DONOR);
        targetUser.setActive(true);

        mockReport = new Report();
        mockReport.setId(900L);
        mockReport.setStatus(ReportStatus.OPEN);
    }

    @Test
    @DisplayName("Admin updates report status successfully")
    void updateReportStatus_Success() {
        ReportStatusUpdateRequest updateReq = new ReportStatusUpdateRequest();
        updateReq.setStatus(ReportStatus.RESOLVED);

        ReportResponse expectedResp = new ReportResponse();
        expectedResp.setId(900L);
        expectedResp.setStatus(ReportStatus.RESOLVED);

        when(reportRepository.findById(900L)).thenReturn(Optional.of(mockReport));
        when(reportRepository.save(any(Report.class))).thenAnswer(i -> i.getArgument(0));
        when(reportService.mapToReportResponse(any())).thenReturn(expectedResp);

        ReportResponse response = adminService.updateReportStatus(900L, adminPrincipal, updateReq);

        assertThat(response.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(mockReport.getReviewedBy()).isEqualTo(adminUser);
    }

    @Test
    @DisplayName("Admin updates user active status successfully")
    void updateUserStatus_Success() {
        AdminUserStatusUpdateRequest updateReq = new AdminUserStatusUpdateRequest();
        updateReq.setActive(false);

        when(userRepository.findById(60L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        AdminUserResponse response = adminService.updateUserStatus(60L, adminPrincipal, updateReq);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("Deactivating the sole active admin throws ResponseStatusException")
    void updateUserStatus_SoleAdmin_ThrowsException() {
        AdminUserStatusUpdateRequest updateReq = new AdminUserStatusUpdateRequest();
        updateReq.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> adminService.updateUserStatus(1L, adminPrincipal, updateReq))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("Get admin dashboard stats")
    void getDashboardStats_Success() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByActiveTrue()).thenReturn(9L);
        when(userRepository.countByRole(Role.DONOR)).thenReturn(5L);
        when(userRepository.countByRole(Role.REQUESTER)).thenReturn(4L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        when(bloodRequestRepository.count()).thenReturn(15L);
        when(bloodRequestRepository.countByStatus(RequestStatus.OPEN)).thenReturn(8L);
        when(bloodRequestRepository.countByStatus(RequestStatus.FULFILLED)).thenReturn(4L);
        when(bloodRequestRepository.countByStatus(RequestStatus.CANCELLED)).thenReturn(3L);

        when(reportRepository.count()).thenReturn(2L);
        when(reportRepository.countByStatus(ReportStatus.OPEN)).thenReturn(1L);
        when(reportRepository.countByStatus(ReportStatus.RESOLVED)).thenReturn(1L);

        AdminDashboardResponse response = adminService.getDashboardStats();

        assertThat(response).isNotNull();
        assertThat(response.getTotalUsers()).isEqualTo(10L);
        assertThat(response.getTotalRequests()).isEqualTo(15L);
        assertThat(response.getTotalReports()).isEqualTo(2L);
    }
}
