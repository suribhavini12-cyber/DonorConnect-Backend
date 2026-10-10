package com.donorconnect.dto;

import java.time.LocalDateTime;

public class RequestEventResponse {

    private Long id;
    private Long requestId;
    private Long actorId;
    private String actorName;
    private String eventType;
    private String description;
    private LocalDateTime createdAt;

    public RequestEventResponse() {}

    public RequestEventResponse(Long id, Long requestId, Long actorId, String actorName, String eventType, String description, LocalDateTime createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.eventType = eventType;
        this.description = description;
        this.createdAt = createdAt;
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

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
