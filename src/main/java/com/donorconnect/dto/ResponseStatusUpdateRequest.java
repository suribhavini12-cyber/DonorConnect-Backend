package com.donorconnect.dto;

import com.donorconnect.model.ResponseStatus;
import jakarta.validation.constraints.NotNull;

public class ResponseStatusUpdateRequest {

    @NotNull(message = "Response status is required")
    private ResponseStatus responseStatus;

    public ResponseStatusUpdateRequest() {}

    public ResponseStatusUpdateRequest(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }
}
