package com.donorconnect.dto;

import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.Urgency;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class BloodRequestRequest {

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotBlank(message = "Blood component is required")
    @Size(max = 100, message = "Blood component name must not exceed 100 characters")
    private String bloodComponent;

    @NotNull(message = "Units required is required")
    @Min(value = 1, message = "Units required must be at least 1")
    @Max(value = 100, message = "Units required must not exceed 100")
    private Integer unitsRequired;

    @NotBlank(message = "Hospital name is required")
    @Size(max = 200, message = "Hospital name must not exceed 200 characters")
    private String hospitalName;

    @NotBlank(message = "Hospital location is required")
    @Size(max = 200, message = "Hospital location must not exceed 200 characters")
    private String hospitalLocation;

    @NotNull(message = "Urgency level is required")
    private Urgency urgency;

    @NotNull(message = "Required by date/time is required")
    @Future(message = "Required by date must be a future date")
    private LocalDateTime requiredBy;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    public BloodRequestRequest() {}

    public BloodRequestRequest(BloodGroup bloodGroup, String bloodComponent, Integer unitsRequired, String hospitalName, String hospitalLocation, Urgency urgency, LocalDateTime requiredBy, String description) {
        this.bloodGroup = bloodGroup;
        this.bloodComponent = bloodComponent;
        this.unitsRequired = unitsRequired;
        this.hospitalName = hospitalName;
        this.hospitalLocation = hospitalLocation;
        this.urgency = urgency;
        this.requiredBy = requiredBy;
        this.description = description;
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
}
