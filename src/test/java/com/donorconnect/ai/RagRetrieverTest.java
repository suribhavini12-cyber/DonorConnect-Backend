
package com.donorconnect.ai;

import com.donorconnect.ai.rag.RagChunk;
import com.donorconnect.ai.rag.RagKnowledgeBase;
import com.donorconnect.ai.rag.RagRetriever;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RagRetrieverTest {

    private RagKnowledgeBase knowledgeBase;
    private RagRetriever retriever;

    private RagChunk donationChunk;
    private RagChunk safetyChunk;

    @BeforeEach
    void setUp() {
        knowledgeBase = mock(RagKnowledgeBase.class);

        donationChunk = new RagChunk(
                "donation-1",
                "donation-process",
                "Donation Process",
                "Donation Basics",
                "The general blood donation process consists of registration, health history and mini-physical, donation, and refreshments and recovery. Donors check in, complete a health screening, donate blood, and rest afterward.",
                "DonorConnect Knowledge Base");

        safetyChunk = new RagChunk(
                "safety-1",
                "preparation-and-safety",
                "Preparation and Safety",
                "Safety",
                "Drink water and eat a healthy meal before donating blood.",
                "DonorConnect Knowledge Base");

        when(knowledgeBase.getChunks())
                .thenReturn(List.of(donationChunk, safetyChunk));

        retriever = new RagRetriever(knowledgeBase);
    }

    @Test
    @DisplayName("Retrieves relevant donation-process information")
    void retrieveTopChunks_RelevantQuestion() {
        List<RagChunk> results = retriever.retrieveTopChunks(
                "How long does blood donation take?", 4);

        assertThat(results).isNotEmpty();
        assertThat(results)
                .anyMatch(chunk -> chunk.getChunkId().equals("donation-1"));
    }

    @Test
    @DisplayName("Retrieves relevant preparation and safety information")
    void retrieveTopChunks_SafetyQuestion() {
        List<RagChunk> results = retriever.retrieveTopChunks(
                "What should I drink before donating blood?", 4);

        assertThat(results)
                .anyMatch(chunk -> chunk.getChunkId().equals("safety-1"));
    }

    @Test
    @DisplayName("Returns no chunks for an unrelated question")
    void retrieveTopChunks_UnrelatedQuestion() {
        List<RagChunk> results = retriever.retrieveTopChunks(
                "What is the capital of Mars?", 4);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Returns no chunks for a blank question")
    void retrieveTopChunks_BlankQuestion() {
        assertThat(retriever.retrieveTopChunks("   ", 4)).isEmpty();
    }

    @Test
    @DisplayName("Matches donating questions with donation content")
    void retrieveTopChunks_DonatingMatchesDonation() {
        List<RagChunk> results = retriever.retrieveTopChunks(
                "What happens when donating blood?", 4);

        assertThat(results)
                .anyMatch(chunk -> chunk.getChunkId().equals("donation-1"));
    }

    @Test
    @DisplayName("Retrieves donation process for a question about starting donation")
    void retrieveTopChunks_HowToStartDonating() {
        List<RagChunk> results = retriever.retrieveTopChunks(
                "What should I do if I want to donate blood?", 4);

        assertThat(results)
                .anyMatch(chunk -> chunk.getChunkId().equals("donation-1"));
    }

}