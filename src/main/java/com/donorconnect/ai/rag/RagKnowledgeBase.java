package com.donorconnect.ai.rag;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RagKnowledgeBase {

    private static final Logger logger = LoggerFactory.getLogger(RagKnowledgeBase.class);

    private final RagChunker chunker;
    private final List<RagDocument> documents = new ArrayList<>();
    private final List<RagChunk> chunks = new ArrayList<>();

    public RagKnowledgeBase(RagChunker chunker) {
        this.chunker = chunker;
    }

    @PostConstruct
    public void init() {
        loadDocuments();
    }

    public void loadDocuments() {
        documents.clear();
        chunks.clear();

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:rag/*.md");

            for (Resource resource : resources) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String fullText = reader.lines().collect(Collectors.joining("\n"));
                    RagDocument doc = parseDocument(resource.getFilename(), fullText);
                    if (doc != null) {
                        documents.add(doc);
                        List<RagChunk> docChunks = chunker.chunkDocument(doc);
                        chunks.addAll(docChunks);
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to load knowledge base document: {}", resource.getFilename(), ex);
                }
            }

            logger.info("Loaded {} RAG documents with {} total chunks.", documents.size(), chunks.size());
        } catch (Exception ex) {
            logger.error("Error loading RAG knowledge base resources", ex);
        }
    }

    private RagDocument parseDocument(String filename, String rawText) {
        String id = filename != null ? filename.replaceAll("\\.md$", "") : "doc-" + System.currentTimeMillis();
        String title = id;
        String topic = "General Information";
        String source = "DonorConnect Knowledge Base";

        StringBuilder body = new StringBuilder();

        String[] lines = rawText.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("Title:")) {
                title = trimmed.substring("Title:".length()).trim();
            } else if (trimmed.startsWith("Topic:")) {
                topic = trimmed.substring("Topic:".length()).trim();
            } else if (trimmed.startsWith("Source:")) {
                source = trimmed.substring("Source:".length()).trim();
            } else if (trimmed.startsWith("# ")) {
                if (title.equals(id)) {
                    title = trimmed.substring(2).trim();
                }
            } else {
                body.append(line).append("\n");
            }
        }

        return new RagDocument(id, title, topic, body.toString().trim(), source);
    }

    public List<RagDocument> getDocuments() {
        return Collections.unmodifiableList(documents);
    }

    public List<RagChunk> getChunks() {
        return Collections.unmodifiableList(chunks);
    }
}
