package com.donorconnect.ai.rag;

public class RagDocument {
    private String id;
    private String title;
    private String topic;
    private String content;
    private String source;

    public RagDocument() {
    }

    public RagDocument(String id, String title, String topic, String content, String source) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.content = content;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
