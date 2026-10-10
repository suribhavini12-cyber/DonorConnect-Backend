package com.donorconnect.ai.rag;

public class RagChunk {
    private String chunkId;
    private String documentId;
    private String title;
    private String topic;
    private String content;
    private String source;

    public RagChunk() {
    }

    public RagChunk(String chunkId, String documentId, String title, String topic, String content, String source) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.title = title;
        this.topic = topic;
        this.content = content;
        this.source = source;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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
