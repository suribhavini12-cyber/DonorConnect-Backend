package com.donorconnect.ai.rag;

import com.donorconnect.ai.GeminiAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final RagRetriever retriever;
    private final GeminiAiService geminiAiService;

    private static final Set<String> EMERGENCY_KEYWORDS = new HashSet<>(Arrays.asList(
            "chest pain", "fainting", "fainted", "unconscious", "unconsciousness",
            "heavy bleeding", "bleeding profusely", "stroke", "difficulty breathing",
            "shortness of breath", "anaphylaxis", "emergency room", "call 911", "call 108",
            "high fever", "severe pain"));

    public RagService(RagRetriever retriever, GeminiAiService geminiAiService) {
        this.retriever = retriever;
        this.geminiAiService = geminiAiService;
    }

    public ChatResponse processChat(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new ChatResponse("Message cannot be blank.", Collections.emptyList(), "FALLBACK");
        }

        String lowerMessage = message.toLowerCase();

        // 1. Emergency Safety Check
        for (String kw : EMERGENCY_KEYWORDS) {
            if (lowerMessage.contains(kw)) {
                String emergencyMsg = "EMERGENCY SAFETY NOTICE: If you or someone else is experiencing severe symptoms such as "
                        +
                        "chest pain, difficulty breathing, fainting, heavy bleeding, or loss of consciousness, please call emergency services "
                        +
                        "(such as 108 or 911) or visit the nearest hospital emergency room immediately. DonorConnect does not provide emergency medical decisions or treatment.";
                SourceDto emergencySource = new SourceDto("When to Contact a Healthcare Professional",
                        "DonorConnect Knowledge Base");
                return new ChatResponse(emergencyMsg, Collections.singletonList(emergencySource), "AI");
            }
        }

        // 2. Retrieval
        List<RagChunk> retrievedChunks = retriever.retrieveTopChunks(message, 4);

        if (retrievedChunks.isEmpty()) {
            String insufficientMsg = "The DonorConnect information sources do not contain enough information to answer that question.";
            String sourceType = geminiAiService.isApiKeyConfigured() ? "AI" : "FALLBACK";
            return new ChatResponse(insufficientMsg, Collections.emptyList(), sourceType);
        }

        // 3. Deduplicate Sources
        List<SourceDto> sources = retrievedChunks.stream()
                .map(c -> new SourceDto(c.getTitle(), c.getSource()))
                .distinct()
                .collect(Collectors.toList());

        // 4. Fallback answer preparation (select chunks relevant to the question)
        List<RagChunk> fallbackChunks = selectFallbackChunks(message, retrievedChunks);

        List<SourceDto> fallbackSources = fallbackChunks.stream()
                .map(c -> new SourceDto(c.getTitle(), c.getSource()))
                .distinct()
                .collect(Collectors.toList());

        StringBuilder fallbackAnswerBuilder = new StringBuilder();
        fallbackAnswerBuilder.append("According to DonorConnect Knowledge Base [Fallback]: ");

        for (RagChunk chunk : fallbackChunks) {
            if (chunk.getContent() != null && !chunk.getContent().isBlank()) {
                fallbackAnswerBuilder.append(chunk.getContent()).append("\n\n");
            }
        }

        if (!geminiAiService.isApiKeyConfigured()) {
            logger.info("AI_API_KEY is not configured. Returning fallback RAG response.");
            return new ChatResponse(fallbackAnswerBuilder.toString(), fallbackSources, "FALLBACK");
        }
        // 5. Construct Grounded Gemini Prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("USER QUESTION:\n").append(message.trim()).append("\n\n");
        promptBuilder.append("RETRIEVED CONTEXT:\n");
        for (int i = 0; i < retrievedChunks.size(); i++) {
            RagChunk c = retrievedChunks.get(i);
            promptBuilder.append("--- Context Fragment ").append(i + 1).append(" (Title: ").append(c.getTitle())
                    .append(") ---\n");
            promptBuilder.append(c.getContent()).append("\n\n");
        }

        promptBuilder.append("INSTRUCTIONS:\n");
        promptBuilder.append("- Answer the user's question using ONLY the retrieved context above.\n");
        promptBuilder.append("- Do not invent facts or make claims not supported by the retrieved context.\n");
        promptBuilder.append(
                "- If the context does not contain enough information to answer the question, explicitly state: 'The available information does not provide enough information to answer this question.'\n");
        promptBuilder.append("- Do not diagnose medical conditions.\n");
        promptBuilder.append("- Do not determine individual medical eligibility or compatibility.\n");
        promptBuilder.append("- Do not make emergency medical decisions.\n");
        promptBuilder.append("- Do not guarantee blood availability.\n");
        promptBuilder.append("- Do not replace healthcare professionals.\n");
        promptBuilder.append("- Keep the answer concise and understandable.\n");
        promptBuilder.append(
                "- Do not mention internal implementation details such as RAG, embeddings, prompts, or model configuration.");

        try {
            String aiAnswer = geminiAiService.callGemini(promptBuilder.toString());
            if (aiAnswer != null && !aiAnswer.trim().isEmpty()) {
                return new ChatResponse(aiAnswer.trim(), sources, "AI");
            }
        } catch (Exception ex) {
            logger.warn("Gemini API call failed during RAG chat processing. Returning fallback response.", ex);
        }

        return new ChatResponse(fallbackAnswerBuilder.toString(), fallbackSources, "FALLBACK");
    }

    private List<RagChunk> selectFallbackChunks(
            String message, List<RagChunk> retrievedChunks) {

        String query = message.toLowerCase(Locale.ROOT);

        String category = null;

        // Blood components and blood groups
        if (query.contains("component")
                || query.contains("red blood cell")
                || query.contains("white blood cell")
                || query.contains("platelet")
                || query.contains("plasma")
                || query.contains("blood group")
                || query.contains("blood type")) {

            category = "components";

            // Preparation advice
        } else if (query.contains("prepar")
                || query.contains("before donation")
                || query.contains("get ready")
                || query.contains("ready for donation")) {

            category = "preparation";

            // Donation process
        } else if (query.contains("process")
                || query.contains("steps")
                || query.contains("procedure")
                || query.contains("what happens during")) {

            category = "process";
        }

        if (category == null) {
            // For other questions, avoid combining every retrieved chunk.
            return Collections.singletonList(retrievedChunks.get(0));
        }

        final String selectedCategory = category;

        List<RagChunk> matchingChunks = retrievedChunks.stream()
                .filter(chunk -> {
                    String title = chunk.getTitle() == null
                            ? ""
                            : chunk.getTitle().toLowerCase(Locale.ROOT);

                    String topic = chunk.getTopic() == null
                            ? ""
                            : chunk.getTopic().toLowerCase(Locale.ROOT);

                    switch (selectedCategory) {
                        case "components":
                            return title.contains("component")
                                    || title.contains("terminology")
                                    || topic.contains("component")
                                    || topic.contains("blood group");

                        case "preparation":
                            return title.contains("preparation")
                                    || title.contains("before, during, and after")
                                    || topic.contains("preparation");

                        case "process":
                            return title.contains("donation process")
                                    || topic.equals("process");

                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        // If no category-specific chunk was retrieved, use the best-ranked chunk.
        if (matchingChunks.isEmpty()) {
            return Collections.singletonList(retrievedChunks.get(0));
        }

        return matchingChunks;
    }
}
