package com.donorconnect.ai.rag;

import java.util.Objects;

public class SourceDto {
    private String title;
    private String source;

    public SourceDto() {
    }

    public SourceDto(String title, String source) {
        this.title = title;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceDto sourceDto = (SourceDto) o;
        return Objects.equals(title, sourceDto.title) && Objects.equals(source, sourceDto.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, source);
    }
}
