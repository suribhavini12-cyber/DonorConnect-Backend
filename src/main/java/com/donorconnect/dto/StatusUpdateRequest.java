package com.donorconnect.dto;

import com.donorconnect.model.RequestStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private RequestStatus status;

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(RequestStatus status) {
        this.status = status;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
