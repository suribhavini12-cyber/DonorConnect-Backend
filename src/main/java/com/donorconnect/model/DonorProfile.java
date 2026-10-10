package com.donorconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_profiles")
public class DonorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "preferred_locations")
    private String preferredLocations;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_status", nullable = false)
    private ParticipationStatus participationStatus = ParticipationStatus.ACTIVE;

    @Column(name = "consent_to_contact", nullable = false)
    private boolean consentToContact = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DonorProfile() {}

    public DonorProfile(User user, BloodGroup bloodGroup, String preferredLocations, ParticipationStatus participationStatus, boolean consentToContact) {
        this.user = user;
        this.bloodGroup = bloodGroup;
        this.preferredLocations = preferredLocations;
        this.participationStatus = participationStatus;
        this.consentToContact = consentToContact;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    @Override
    public String toString() {
        return "DonorProfile{" +
                "id=" + id +
                ", bloodGroup=" + bloodGroup +
                ", preferredLocations='" + preferredLocations + '\'' +
                ", participationStatus=" + participationStatus +
                ", consentToContact=" + consentToContact +
                '}';
    }
}
