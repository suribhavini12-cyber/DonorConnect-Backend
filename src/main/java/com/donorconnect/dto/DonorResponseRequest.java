package com.donorconnect.dto;

import jakarta.validation.constraints.Size;

public class DonorResponseRequest {

    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    public DonorResponseRequest() {}

    public DonorResponseRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
