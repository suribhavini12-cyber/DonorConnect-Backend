package com.donorconnect.dto;

import com.donorconnect.model.ReportStatus;
import java.time.LocalDateTime;

public class ReportResponse {

    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long targetUserId;
    private String targetUserName;
    private Long targetRequestId;
    private String reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private Long reviewedById;
    private String reviewedByName;

    public ReportResponse() {}

    public ReportResponse(Long id, Long reporterId, String reporterName, Long targetUserId, String targetUserName, Long targetRequestId, String reason, String description, ReportStatus status, LocalDateTime createdAt, LocalDateTime reviewedAt, Long reviewedById, String reviewedByName) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.targetUserId = targetUserId;
        this.targetUserName = targetUserName;
        this.targetRequestId = targetRequestId;
        this.reason = reason;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
        this.reviewedById = reviewedById;
        this.reviewedByName = reviewedByName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public Long getTargetRequestId() {
        return targetRequestId;
    }

    public void setTargetRequestId(Long targetRequestId) {
        this.targetRequestId = targetRequestId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Long getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Long reviewedById) {
        this.reviewedById = reviewedById;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }
}
