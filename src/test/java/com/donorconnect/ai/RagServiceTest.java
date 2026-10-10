package com.donorconnect.ai;

import com.donorconnect.ai.rag.ChatResponse;
import com.donorconnect.ai.rag.RagChunk;
import com.donorconnect.ai.rag.RagRetriever;
import com.donorconnect.ai.rag.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private RagRetriever retriever;

    @Mock
    private GeminiAiService geminiAiService;

    @InjectMocks
    private RagService ragService;

    private RagChunk mockChunk;

    @BeforeEach
    void setUp() {
        mockChunk = new RagChunk("c1", "doc1", "General Process", "Basics", "Donation takes 45-60 mins.", "DonorConnect Knowledge Base");
    }

    @Test
    @DisplayName("Blank message returns fallback warning")
    void processChat_BlankMessage() {
        ChatResponse response = ragService.processChat("   ");
        assertThat(response.getAnswer()).contains("cannot be blank");
    }

    @Test
    @DisplayName("Emergency question returns immediate medical safety warning")
    void processChat_EmergencyQuestion() {
        ChatResponse response = ragService.processChat("I am having severe chest pain and heavy bleeding after donation!");

        assertThat(response.getAnswer()).contains("EMERGENCY SAFETY NOTICE");
        assertThat(response.getAnswer()).contains("108");
        assertThat(response.getSources()).hasSize(1);
    }

    @Test
    @DisplayName("Relevant question retrieves context and returns fallback when key missing")
    void processChat_RelevantQuestion() {
        when(retriever.retrieveTopChunks("How long does donation take?", 4)).thenReturn(List.of(mockChunk));
        when(geminiAiService.isApiKeyConfigured()).thenReturn(false);

        ChatResponse response = ragService.processChat("How long does donation take?");

        assertThat(response.getSourceType()).isEqualTo("FALLBACK");
        assertThat(response.getAnswer()).contains("Donation takes 45-60 mins.");
        assertThat(response.getSources()).hasSize(1);
    }

    @Test
    @DisplayName("Irrelevant question returns insufficient information message")
    void processChat_IrrelevantQuestion() {
        when(retriever.retrieveTopChunks(anyString(), anyInt())).thenReturn(Collections.emptyList());
        when(geminiAiService.isApiKeyConfigured()).thenReturn(false);

        ChatResponse response = ragService.processChat("What is the capital of Mars?");

        assertThat(response.getAnswer()).contains("do not contain enough information");
        assertThat(response.getSources()).isEmpty();
    }
}
