package com.donorconnect.dto;

import com.donorconnect.model.ReportStatus;
import jakarta.validation.constraints.NotNull;

public class ReportStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ReportStatus status;

    public ReportStatusUpdateRequest() {}

    public ReportStatusUpdateRequest(ReportStatus status) {
        this.status = status;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
}
