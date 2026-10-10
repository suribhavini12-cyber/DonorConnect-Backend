package com.donorconnect.ai;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AiRequestSummaryRequest {

    @NotNull(message = "Request ID is required")
    @Positive(message = "Request ID must be positive")
    private Long requestId;

    public AiRequestSummaryRequest() {}

    public AiRequestSummaryRequest(Long requestId) {
        this.requestId = requestId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
}
