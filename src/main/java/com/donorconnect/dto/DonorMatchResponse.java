package com.donorconnect.dto;

import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.ParticipationStatus;

public class DonorMatchResponse {

    private Long donorId;
    private String donorName;
    private BloodGroup bloodGroup;
    private String preferredLocations;
    private ParticipationStatus participationStatus;
    private boolean consentToContact;
    private String matchReason;

    public DonorMatchResponse() {}

    public DonorMatchResponse(Long donorId, String donorName, BloodGroup bloodGroup, String preferredLocations, ParticipationStatus participationStatus, boolean consentToContact, String matchReason) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.bloodGroup = bloodGroup;
        this.preferredLocations = preferredLocations;
        this.participationStatus = participationStatus;
        this.consentToContact = consentToContact;
        this.matchReason = matchReason;
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

    public boolean isConsentToContact() {
        return consentToContact;
    }

    public void setConsentToContact(boolean consentToContact) {
        this.consentToContact = consentToContact;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }
}
