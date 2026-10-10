package com.donorconnect.ai.rag;

import java.util.List;

public class ChatResponse {
    private String answer;
    private List<SourceDto> sources;
    private String sourceType;

    public ChatResponse() {
    }

    public ChatResponse(String answer, List<SourceDto> sources, String sourceType) {
        this.answer = answer;
        this.sources = sources;
        this.sourceType = sourceType;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<SourceDto> getSources() {
        return sources;
    }

    public void setSources(List<SourceDto> sources) {
        this.sources = sources;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
