package com.donorconnect.dto;

import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.RequestStatus;
import com.donorconnect.model.Urgency;

import java.time.LocalDateTime;

public class BloodRequestResponse {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private BloodGroup bloodGroup;
    private String bloodComponent;
    private Integer unitsRequired;
    private String hospitalName;
    private String hospitalLocation;
    private Urgency urgency;
    private LocalDateTime requiredBy;
    private String description;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BloodRequestResponse() {}

    public BloodRequestResponse(Long id, Long requesterId, String requesterName, BloodGroup bloodGroup, String bloodComponent, Integer unitsRequired, String hospitalName, String hospitalLocation, Urgency urgency, LocalDateTime requiredBy, String description, RequestStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.bloodGroup = bloodGroup;
        this.bloodComponent = bloodComponent;
        this.unitsRequired = unitsRequired;
        this.hospitalName = hospitalName;
        this.hospitalLocation = hospitalLocation;
        this.urgency = urgency;
        this.requiredBy = requiredBy;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getBloodComponent() {
        return bloodComponent;
    }

    public void setBloodComponent(String bloodComponent) {
        this.bloodComponent = bloodComponent;
    }

    public Integer getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(Integer unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalLocation() {
        return hospitalLocation;
    }

    public void setHospitalLocation(String hospitalLocation) {
        this.hospitalLocation = hospitalLocation;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime getRequiredBy() {
        return requiredBy;
    }

    public void setRequiredBy(LocalDateTime requiredBy) {
        this.requiredBy = requiredBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
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
