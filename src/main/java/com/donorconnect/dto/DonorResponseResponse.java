package com.donorconnect.dto;

import com.donorconnect.model.ResponseStatus;
import java.time.LocalDateTime;

public class DonorResponseResponse {

    private Long id;
    private Long requestId;
    private Long donorId;
    private String donorName;
    private ResponseStatus responseStatus;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DonorResponseResponse() {}

    public DonorResponseResponse(Long id, Long requestId, Long donorId, String donorName, ResponseStatus responseStatus, String message, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.requestId = requestId;
        this.donorId = donorId;
        this.donorName = donorName;
        this.responseStatus = responseStatus;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
