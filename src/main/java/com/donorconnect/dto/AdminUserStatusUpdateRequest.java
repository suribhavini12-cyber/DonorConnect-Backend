package com.donorconnect.dto;

import jakarta.validation.constraints.NotNull;

public class AdminUserStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;

    public AdminUserStatusUpdateRequest() {}

    public AdminUserStatusUpdateRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
