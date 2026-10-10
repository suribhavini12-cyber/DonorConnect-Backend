package com.donorconnect.dto;

import com.donorconnect.model.ParticipationStatus;
import jakarta.validation.constraints.NotNull;

public class ParticipationUpdateRequest {

    @NotNull(message = "Participation status is required")
    private ParticipationStatus participationStatus;

    public ParticipationUpdateRequest() {}

    public ParticipationUpdateRequest(ParticipationStatus participationStatus) {
        this.participationStatus = participationStatus;
    }

    public ParticipationStatus getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(ParticipationStatus participationStatus) {
        this.participationStatus = participationStatus;
    }
}
