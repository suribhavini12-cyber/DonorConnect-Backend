package com.donorconnect.service;

import com.donorconnect.dto.ReportRequest;
import com.donorconnect.dto.ReportResponse;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.Report;
import com.donorconnect.model.ReportStatus;
import com.donorconnect.model.User;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.ReportRepository;
import com.donorconnect.repository.UserRepository;
import com.donorconnect.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BloodRequestRepository bloodRequestRepository;

    public ReportService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            BloodRequestRepository bloodRequestRepository
    ) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.bloodRequestRepository = bloodRequestRepository;
    }

    @Transactional
    public ReportResponse createReport(UserPrincipal principal, ReportRequest requestDto) {
        User reporter = principal.getUser();

        if (requestDto.getTargetUserId() == null && requestDto.getTargetRequestId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one target (user or request) must be specified."
            );
        }

        User targetUser = null;
        if (requestDto.getTargetUserId() != null) {
            targetUser = userRepository.findById(requestDto.getTargetUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Target user not found with ID: " + requestDto.getTargetUserId()
                    ));
        }

        BloodRequest targetRequest = null;
        if (requestDto.getTargetRequestId() != null) {
            targetRequest = bloodRequestRepository.findById(requestDto.getTargetRequestId())
                    .orElseThrow(() -> new RequestNotFoundException(
                            "Target blood request not found with ID: " + requestDto.getTargetRequestId()
                    ));
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetUser(targetUser);
        report.setTargetRequest(targetRequest);
        report.setReason(requestDto.getReason().trim());
        report.setDescription(requestDto.getDescription().trim());
        report.setStatus(ReportStatus.OPEN);

        Report savedReport = reportRepository.save(report);
        return mapToReportResponse(savedReport);
    }

    public ReportResponse mapToReportResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                report.getReporter() != null ? report.getReporter().getName() : null,
                report.getTargetUser() != null ? report.getTargetUser().getId() : null,
                report.getTargetUser() != null ? report.getTargetUser().getName() : null,
                report.getTargetRequest() != null ? report.getTargetRequest().getId() : null,
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt(),
                report.getReviewedBy() != null ? report.getReviewedBy().getId() : null,
                report.getReviewedBy() != null ? report.getReviewedBy().getName() : null
        );
    }
}
