package com.donorconnect.ai;

import java.util.List;

public class DuplicateCheckResponse {

    private Long requestId;
    private boolean possibleDuplicate;
    private Double confidence;
    private List<Long> relatedRequestIds;
    private String explanation;
    private String source; // "AI" or "FALLBACK"

    public DuplicateCheckResponse() {}

    public DuplicateCheckResponse(Long requestId, boolean possibleDuplicate, Double confidence, List<Long> relatedRequestIds, String explanation, String source) {
        this.requestId = requestId;
        this.possibleDuplicate = possibleDuplicate;
        this.confidence = confidence;
        this.relatedRequestIds = relatedRequestIds;
        this.explanation = explanation;
        this.source = source;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public boolean isPossibleDuplicate() {
        return possibleDuplicate;
    }

    public void setPossibleDuplicate(boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public List<Long> getRelatedRequestIds() {
        return relatedRequestIds;
    }

    public void setRelatedRequestIds(List<Long> relatedRequestIds) {
        this.relatedRequestIds = relatedRequestIds;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
