package com.donorconnect.ai.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RagChunker {

    public List<RagChunk> chunkDocument(RagDocument doc) {
        List<RagChunk> chunks = new ArrayList<>();
        if (doc == null || doc.getContent() == null || doc.getContent().trim().isEmpty()) {
            return chunks;
        }

        // Split by double newlines or section breaks
        String[] paragraphs = doc.getContent().split("\\n\\s*\\n");
        int chunkIndex = 1;

        StringBuilder currentChunk = new StringBuilder();
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            // Header lines or small paragraphs can be grouped, or paragraphs can form distinct chunks
            if (currentChunk.length() + trimmed.length() > 600 && currentChunk.length() > 0) {
                String chunkId = doc.getId() + "-chunk-" + chunkIndex++;
                chunks.add(new RagChunk(chunkId, doc.getId(), doc.getTitle(), doc.getTopic(), currentChunk.toString().trim(), doc.getSource()));
                currentChunk.setLength(0);
            }

            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(trimmed);
        }

        if (currentChunk.length() > 0) {
            String chunkId = doc.getId() + "-chunk-" + chunkIndex;
            chunks.add(new RagChunk(chunkId, doc.getId(), doc.getTitle(), doc.getTopic(), currentChunk.toString().trim(), doc.getSource()));
        }

        return chunks;
    }
}
