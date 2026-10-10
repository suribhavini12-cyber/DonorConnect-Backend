package com.donorconnect.dto;

import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.ParticipationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DonorProfileRequest {

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotBlank(message = "Preferred locations are required")
    private String preferredLocations;

    @NotNull(message = "Participation status is required")
    private ParticipationStatus participationStatus;

    @NotNull(message = "Consent to contact is required")
    private Boolean consentToContact;

    public DonorProfileRequest() {}

    public DonorProfileRequest(BloodGroup bloodGroup, String preferredLocations, ParticipationStatus participationStatus, Boolean consentToContact) {
        this.bloodGroup = bloodGroup;
        this.preferredLocations = preferredLocations;
        this.participationStatus = participationStatus;
        this.consentToContact = consentToContact;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPreferredLocations() {
        return preferredLocations;
    }

    public void setPreferredLocations(String preferredLocations) {
        this.preferredLocations = preferredLocations;
    }

    public ParticipationStatus getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(ParticipationStatus participationStatus) {
        this.participationStatus = participationStatus;
    }

    public Boolean getConsentToContact() {
        return consentToContact;
    }

    public void setConsentToContact(Boolean consentToContact) {
        this.consentToContact = consentToContact;
    }
}
