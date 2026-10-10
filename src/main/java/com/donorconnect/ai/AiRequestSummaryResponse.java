package com.donorconnect.ai;

public class AiRequestSummaryResponse {

    private Long requestId;
    private String summary;
    private String source; // "AI" or "FALLBACK"

    public AiRequestSummaryResponse() {}

    public AiRequestSummaryResponse(Long requestId, String summary, String source) {
        this.requestId = requestId;
        this.summary = summary;
        this.source = source;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
