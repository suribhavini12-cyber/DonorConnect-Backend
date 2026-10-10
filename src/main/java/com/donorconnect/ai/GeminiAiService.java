package com.donorconnect.ai;

import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.RequestStatus;
import com.donorconnect.model.Role;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiAiService implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAiService.class);

    private final BloodRequestRepository bloodRequestRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-3.8-flash}")
    private String model;

    @Value("${ai.gemini.baseUrl:https://generativelanguage.googleapis.com/v1beta/models/}")
    private String baseUrl;

    public GeminiAiService(
            BloodRequestRepository bloodRequestRepository,
            RestTemplate restTemplate) {

        this.bloodRequestRepository = bloodRequestRepository;
        this.restTemplate = restTemplate;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public String callGemini(String prompt) {
        if (!isApiKeyConfigured()) {
            return null;
        }

        return callGeminiApi(prompt);
    }

    @Override
    @Transactional(readOnly = true)
    public AiRequestSummaryResponse generateSummary(
            UserPrincipal principal,
            Long requestId) {

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(
                        "Blood request not found with ID: " + requestId));

        boolean isOwner = request.getRequester().getId()
                .equals(principal.getUser().getId());

        boolean isAdmin = principal.getUser().getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not authorized to view AI summary for this request.");
        }

        String templateSummary = String.format(
                "A %s urgency request for %s blood (%s), requiring %d unit(s) at %s in %s.",
                request.getUrgency(),
                request.getBloodGroup(),
                request.getBloodComponent(),
                request.getUnitsRequired(),
                request.getHospitalName(),
                request.getHospitalLocation());

        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.info(
                    "AI_API_KEY is not configured. Returning fallback summary for request {}",
                    requestId);

            return new AiRequestSummaryResponse(
                    requestId,
                    templateSummary,
                    "FALLBACK");
        }

        try {
            String prompt = String.format(
                    "Summarize the following blood request in 1-2 concise sentences. "
                            + "Summarize ONLY the provided fields. "
                            + "Do NOT invent missing information, medical claims, "
                            + "blood availability, or donor eligibility.\n"
                            + "Request ID: %d\n"
                            + "Blood Group: %s\n"
                            + "Component: %s\n"
                            + "Units: %d\n"
                            + "Hospital: %s\n"
                            + "Location: %s\n"
                            + "Urgency: %s\n"
                            + "Description: %s",

                    request.getId(),
                    request.getBloodGroup(),
                    request.getBloodComponent(),
                    request.getUnitsRequired(),
                    request.getHospitalName(),
                    request.getHospitalLocation(),
                    request.getUrgency(),
                    request.getDescription() != null
                            ? request.getDescription()
                            : "None");

            String aiResponse = callGeminiApi(prompt);

            if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                return new AiRequestSummaryResponse(
                        requestId,
                        aiResponse.trim(),
                        "AI");
            }

        } catch (RestClientException ex) {

            logger.warn(
                    "Gemini API call failed for request {}. "
                            + "Returning fallback response. Cause: {}",
                    requestId,
                    ex.getMessage());

        } catch (Exception ex) {

            logger.warn(
                    "Unexpected error during Gemini summary generation "
                            + "for request {}. Returning fallback response.",
                    requestId,
                    ex);
        }

        return new AiRequestSummaryResponse(
                requestId,
                templateSummary,
                "FALLBACK");
    }

    @Override
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkForDuplicates(
            UserPrincipal principal,
            Long requestId) {

        BloodRequest targetRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(
                        "Blood request not found with ID: " + requestId));

        boolean isOwner = targetRequest.getRequester().getId()
                .equals(principal.getUser().getId());

        boolean isAdmin = principal.getUser().getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not authorized to perform duplicate check on this request.");
        }

        List<BloodRequest> candidates = bloodRequestRepository.findAll()
                .stream()
                .filter(r -> !r.getId().equals(requestId))
                .filter(r -> r.getStatus() == RequestStatus.OPEN
                        || r.getStatus() == RequestStatus.PENDING_REVIEW
                        || r.getStatus() == RequestStatus.PARTIALLY_RESPONDED)
                .limit(10)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return new DuplicateCheckResponse(
                    requestId,
                    false,
                    0.0,
                    Collections.emptyList(),
                    "No other active blood requests found for duplicate comparison.",
                    "FALLBACK");
        }

        List<Long> matchedIds = new ArrayList<>();

        String targetLoc = targetRequest.getHospitalLocation() != null
                ? targetRequest.getHospitalLocation().trim().toLowerCase()
                : "";

        for (BloodRequest candidate : candidates) {

            if (candidate.getBloodGroup() == targetRequest.getBloodGroup()) {

                String candLoc = candidate.getHospitalLocation() != null
                        ? candidate.getHospitalLocation().trim().toLowerCase()
                        : "";

                if (!targetLoc.isEmpty()
                        && !candLoc.isEmpty()
                        && (targetLoc.contains(candLoc)
                                || candLoc.contains(targetLoc))) {

                    matchedIds.add(candidate.getId());
                }
            }
        }

        boolean isDuplicate = !matchedIds.isEmpty();

        double confidence = isDuplicate ? 0.85 : 0.10;

        String explanation = isDuplicate
                ? "Potential duplicate detected: Found active request(s) "
                        + matchedIds
                        + " with matching blood group ("
                        + targetRequest.getBloodGroup()
                        + ") and hospital location ("
                        + targetRequest.getHospitalLocation()
                        + ")."

                : "No matching blood group and hospital location duplicates "
                        + "detected among active requests.";

        String source = "FALLBACK";

        if (apiKey != null && !apiKey.trim().isEmpty()) {

            try {

                StringBuilder promptBuilder = new StringBuilder();

                promptBuilder.append(
                        "Compare target blood request ID ")
                        .append(requestId)
                        .append(
                                " against candidate active requests to detect "
                                        + "potential duplicates. "
                                        + "Duplicate detection is strictly advisory.\n");

                promptBuilder.append(
                        "Target: Group: ")
                        .append(targetRequest.getBloodGroup())
                        .append(", Hospital: ")
                        .append(targetRequest.getHospitalName())
                        .append(", Location: ")
                        .append(targetRequest.getHospitalLocation())
                        .append("\n");

                promptBuilder.append("Candidates: ")
                        .append(
                                candidates.stream()
                                        .map(c -> "#" + c.getId()
                                                + " ("
                                                + c.getBloodGroup()
                                                + ", "
                                                + c.getHospitalLocation()
                                                + ")")
                                        .collect(Collectors.joining("; ")))
                        .append("\n");

                promptBuilder.append(
                        "Reply in at most 40 words. Assess whether the target request "
                                + "may duplicate a candidate. Consider blood group, "
                                + "hospital and location. Be concise.");

                String aiResult = callGeminiApi(
                        promptBuilder.toString());

                if (aiResult != null
                        && !aiResult.trim().isEmpty()) {

                    source = "AI";
                    logger.info("Gemini duplicate analysis succeeded for request {}", requestId);

                    explanation = aiResult.trim()
                            + " (Advisory comparison against active requests: "
                            + matchedIds
                            + ")";
                }

            } catch (Exception ex) {

                logger.warn(
                        "Gemini API duplicate check failed for request {}. "
                                + "Falling back to rule-based evaluation. "
                                + "Cause: {}",
                        requestId,
                        ex.getMessage());

            }
        }

        return new DuplicateCheckResponse(
                requestId,
                isDuplicate,
                confidence,
                matchedIds,
                explanation,
                source);
    }

    private String callGeminiApi(String prompt) {

        String endpoint = baseUrl
                + model
                + ":generateContent";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        headers.set(
                "x-goog-api-key",
                apiKey);

        Map<String, Object> textPart = new HashMap<>();

        textPart.put(
                "text",
                prompt);

        Map<String, Object> partsObj = new HashMap<>();

        partsObj.put(
                "parts",
                Collections.singletonList(textPart));

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put(
                "contents",
                Collections.singletonList(partsObj));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                requestBody,
                headers);

        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        endpoint,
                        entity,
                        Map.class);

                if (response.getStatusCode().is2xxSuccessful()
                        && response.getBody() != null) {

                    Map body = response.getBody();

                    List candidates = (List) body.get("candidates");

                    if (candidates != null
                            && !candidates.isEmpty()) {

                        Map firstCand = (Map) candidates.get(0);

                        Map content = (Map) firstCand.get("content");

                        if (content != null) {

                            List parts = (List) content.get("parts");

                            if (parts != null
                                    && !parts.isEmpty()) {

                                Map firstPart = (Map) parts.get(0);

                                return (String) firstPart.get("text");
                            }
                        }
                    }
                }

                return null;

            } catch (HttpServerErrorException ex) {

                logger.warn(
                        "Gemini returned {} on attempt {}/{}. Retrying...",
                        ex.getStatusCode().value(),
                        attempt,
                        maxAttempts);

                if (attempt < maxAttempts) {

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }

            } catch (RestClientException ex) {

                logger.warn(
                        "HTTP call to Gemini endpoint failed: status/message={}",
                        ex.getMessage());

                throw ex;
            }
        }

        return null;
    }
}