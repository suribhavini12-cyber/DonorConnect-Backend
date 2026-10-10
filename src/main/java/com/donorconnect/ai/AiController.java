package com.donorconnect.ai;

import com.donorconnect.ai.rag.ChatRequest;
import com.donorconnect.ai.rag.ChatResponse;
import com.donorconnect.ai.rag.RagService;
import com.donorconnect.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI & RAG Services", description = "Gemini AI blood request summary, AI duplicate request advisory, and grounded RAG chatbot endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;
    private final RagService ragService;

    public AiController(AiService aiService, RagService ragService) {
        this.aiService = aiService;
        this.ragService = ragService;
    }

    @PostMapping("/request-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate AI request summary", description = "Generates a structured summary for a blood request using Gemini AI (or safe fallback). Does not invent facts or alter original request data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI summary generated"),
            @ApiResponse(responseCode = "403", description = "Forbidden — User is neither request owner nor ADMIN"),
            @ApiResponse(responseCode = "404", description = "Blood request not found")
    })
    public ResponseEntity<AiRequestSummaryResponse> generateSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AiRequestSummaryRequest request
    ) {
        AiRequestSummaryResponse response = aiService.generateSummary(principal, request.getRequestId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/duplicate-check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Perform AI duplicate request check", description = "Analyzes potential duplicate blood requests using Gemini AI. NOTE: Results are advisory only and do NOT automatically modify or cancel requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Duplicate check advisory generated"),
            @ApiResponse(responseCode = "403", description = "Forbidden — User is neither request owner nor ADMIN"),
            @ApiResponse(responseCode = "404", description = "Blood request not found")
    })
    public ResponseEntity<DuplicateCheckResponse> checkForDuplicates(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DuplicateCheckRequest request
    ) {
        DuplicateCheckResponse response = aiService.checkForDuplicates(principal, request.getRequestId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ask RAG donation chatbot", description = "Answers blood donation queries grounded in approved knowledge base chunks. NOTE: Informational guidance only; does NOT replace medical professionals.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat response generated with retrieved knowledge sources"),
            @ApiResponse(responseCode = "400", description = "Blank or invalid message query (>1000 chars)")
    })
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChatRequest request
    ) {
        ChatResponse response = ragService.processChat(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
