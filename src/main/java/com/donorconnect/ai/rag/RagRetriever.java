package com.donorconnect.ai.rag;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RagRetriever {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in", "is", "it",
            "its", "of", "on", "that", "the", "to", "was", "were", "will", "with", "what", "where", "when",
            "who", "how", "can", "should", "could", "would", "do", "does", "did", "i", "you", "me", "my",
            "we", "our", "tell", "about", "give", "please", "some", "any", "is", "capital", "space", "travel", "mars"));

    private final RagKnowledgeBase knowledgeBase;

    public RagRetriever(RagKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public List<RagChunk> retrieveTopChunks(String query, int topN) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> queryTokens = tokenizeAndNormalize(query);
        if (queryTokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredChunk> scoredChunks = new ArrayList<>();

        for (RagChunk chunk : knowledgeBase.getChunks()) {
            double score = calculateScore(queryTokens, chunk);
            if (score > 0.0) {
                scoredChunks.add(new ScoredChunk(chunk, score));
            }
        }

        // Sort by score descending
        scoredChunks.sort((a, b) -> Double.compare(b.score, a.score));

        // Minimum relevance threshold: requiring score >= 3.0 for meaningful relevance
        double minThreshold = 3.0;
        List<RagChunk> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, scoredChunks.size()); i++) {
            ScoredChunk sc = scoredChunks.get(i);
            if (sc.score >= minThreshold) {
                result.add(sc.chunk);
            }
        }

        return result;
    }

    private double calculateScore(List<String> queryTokens, RagChunk chunk) {
        String contentLower = chunk.getContent() != null ? chunk.getContent().toLowerCase() : "";
        String titleLower = chunk.getTitle() != null ? chunk.getTitle().toLowerCase() : "";
        String topicLower = chunk.getTopic() != null ? chunk.getTopic().toLowerCase() : "";

        double totalScore = 0.0;
        int matchedTokens = 0;

        for (String token : queryTokens) {
            boolean matched = false;
            // Use word boundary matching for exact tokens
            String regex = "\\b" + java.util.regex.Pattern.quote(token) + "\\b";

            if (java.util.regex.Pattern.compile(regex).matcher(contentLower).find()) {
                totalScore += 2.0;
                matched = true;
            }
            if (java.util.regex.Pattern.compile(regex).matcher(titleLower).find()) {
                totalScore += 4.0;
                matched = true;
            }
            if (java.util.regex.Pattern.compile(regex).matcher(topicLower).find()) {
                totalScore += 3.0;
                matched = true;
            }

            if (matched) {
                matchedTokens++;
            }
        }

        // Bonus for multi-term query coverage
        if (matchedTokens > 1) {
            totalScore *= (1.0 + 0.3 * (matchedTokens - 1));
        }

        return totalScore;
    }

    private List<String> tokenizeAndNormalize(String text) {
        String cleaned = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ");
        String[] words = cleaned.split("\\s+");

        List<String> tokens = new ArrayList<>();

        for (String word : words) {
            String trimmed = word.trim();

            if (trimmed.length() <= 1 || STOP_WORDS.contains(trimmed)) {
                continue;
            }

            // Normalize common English word variations.
            String normalized = trimmed;

            if (normalized.length() > 5 && normalized.endsWith("ies")) {
                normalized = normalized.substring(0, normalized.length() - 3) + "y";
            } else if (normalized.length() > 5 && normalized.endsWith("ing")) {
                normalized = normalized.substring(0, normalized.length() - 3);
            } else if (normalized.length() > 4 && normalized.endsWith("ed")) {
                normalized = normalized.substring(0, normalized.length() - 2);
            } else if (normalized.length() > 4 && normalized.endsWith("es")) {
                normalized = normalized.substring(0, normalized.length() - 2);
            } else if (normalized.length() > 3 && normalized.endsWith("s")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }

            if (!normalized.isEmpty()) {
                tokens.add(normalized);
            }
        }

        return tokens;
    }

    private static class ScoredChunk {
        final RagChunk chunk;
        final double score;

        ScoredChunk(RagChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
