package com.donorconnect.controller;

import com.donorconnect.ai.*;
import com.donorconnect.ai.rag.ChatRequest;
import com.donorconnect.ai.rag.ChatResponse;
import com.donorconnect.ai.rag.RagService;
import com.donorconnect.ai.rag.SourceDto;
import com.donorconnect.security.JwtAuthenticationFilter;
import com.donorconnect.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiService aiService;

    @MockitoBean
    private RagService ragService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/ai/request-summary returns summary response")
    void generateSummary_Success() throws Exception {
        AiRequestSummaryRequest req = new AiRequestSummaryRequest();
        req.setRequestId(100L);

        AiRequestSummaryResponse resp = new AiRequestSummaryResponse(100L, "Sample summary text", "FALLBACK");

        when(aiService.generateSummary(any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/ai/request-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(100))
                .andExpect(jsonPath("$.source").value("FALLBACK"));
    }

    @Test
    @DisplayName("POST /api/ai/chat returns grounded RAG chat answer")
    void chat_Success() throws Exception {
        ChatRequest req = new ChatRequest("What is blood donation?");

        SourceDto s = new SourceDto("General Blood Donation Process", "DonorConnect Knowledge Base");
        ChatResponse resp = new ChatResponse("Blood donation is voluntary...", List.of(s), "AI");

        when(ragService.processChat("What is blood donation?")).thenReturn(resp);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceType").value("AI"))
                .andExpect(jsonPath("$.sources[0].title").value("General Blood Donation Process"));
    }

    @Test
    @DisplayName("POST /api/ai/chat with blank message returns 400 Bad Request")
    void chat_BlankMessage_Returns400() throws Exception {
        ChatRequest req = new ChatRequest("   ");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
