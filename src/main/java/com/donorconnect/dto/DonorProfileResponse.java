package com.donorconnect.dto;

import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.ParticipationStatus;

import java.time.LocalDateTime;

public class DonorProfileResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private BloodGroup bloodGroup;
    private String preferredLocations;
    private ParticipationStatus participationStatus;
    private boolean consentToContact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DonorProfileResponse() {}

    public DonorProfileResponse(Long id, Long userId, String name, String email, BloodGroup bloodGroup, String preferredLocations, ParticipationStatus participationStatus, boolean consentToContact, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.preferredLocations = preferredLocations;
        this.participationStatus = participationStatus;
        this.consentToContact = consentToContact;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
