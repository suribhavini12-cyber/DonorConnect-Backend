package com.donorconnect.service;

import com.donorconnect.dto.ReportRequest;
import com.donorconnect.dto.ReportResponse;
import com.donorconnect.model.Report;
import com.donorconnect.model.ReportStatus;
import com.donorconnect.model.Role;
import com.donorconnect.model.User;
import com.donorconnect.model.BloodRequest;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.ReportRepository;
import com.donorconnect.repository.UserRepository;
import com.donorconnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @InjectMocks
    private ReportService reportService;

    private User reporterUser;
    private UserPrincipal principal;
    private Report mockReport;
    private BloodRequest mockBloodRequest;

    @BeforeEach
    void setUp() {
        reporterUser = new User();
        reporterUser.setId(50L);
        reporterUser.setName("Reporter User");
        reporterUser.setRole(Role.DONOR);
        principal = new UserPrincipal(reporterUser);

        mockBloodRequest = new BloodRequest();
        mockBloodRequest.setId(123L);

        mockReport = new Report();
        mockReport.setId(800L);
        mockReport.setReporter(reporterUser);
        mockReport.setTargetRequest(mockBloodRequest);
        mockReport.setReason("SPAM");
        mockReport.setDescription("Suspicious request details");
        mockReport.setStatus(ReportStatus.OPEN);
    }

    @Test
    @DisplayName("Authenticated user creates a moderation report")
    void createReport_Success() {
        ReportRequest request = new ReportRequest();
        request.setReason("SPAM");
        request.setDescription("Suspicious request details");
        request.setTargetRequestId(123L);

        when(bloodRequestRepository.findById(123L)).thenReturn(Optional.of(mockBloodRequest));
        when(reportRepository.save(any(Report.class))).thenReturn(mockReport);

        ReportResponse response = reportService.createReport(principal, request);

        assertThat(response).isNotNull();
        assertThat(response.getReason()).isEqualTo("SPAM");
        assertThat(response.getStatus()).isEqualTo(ReportStatus.OPEN);
        verify(reportRepository).save(any(Report.class));
    }
}
