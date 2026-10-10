package com.donorconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportRequest {

    private Long targetUserId;
    private Long targetRequestId;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    public ReportRequest() {}

    public ReportRequest(Long targetUserId, Long targetRequestId, String reason, String description) {
        this.targetUserId = targetUserId;
        this.targetRequestId = targetRequestId;
        this.reason = reason;
        this.description = description;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
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
}
