package com.donorconnect.service;

import com.donorconnect.dto.DonorResponseRequest;
import com.donorconnect.dto.DonorResponseResponse;
import com.donorconnect.dto.ResponseStatusUpdateRequest;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.*;
import com.donorconnect.repository.*;
import com.donorconnect.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorResponseService {

    private final DonorResponseRepository donorResponseRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final RequestEventRepository requestEventRepository;

    public DonorResponseService(
            DonorResponseRepository donorResponseRepository,
            BloodRequestRepository bloodRequestRepository,
            DonorProfileRepository donorProfileRepository,
            RequestEventRepository requestEventRepository
    ) {
        this.donorResponseRepository = donorResponseRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.requestEventRepository = requestEventRepository;
    }

    @Transactional
    public DonorResponseResponse createResponse(Long requestId, UserPrincipal principal, DonorResponseRequest requestDto) {
        User user = principal.getUser();
        if (user.getRole() != Role.DONOR) {
            throw new AccessDeniedException("Only donor users can create a response.");
        }

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + requestId));

        if (request.getStatus() != RequestStatus.OPEN && request.getStatus() != RequestStatus.PARTIALLY_RESPONDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blood request is not currently accepting responses.");
        }

        DonorProfile profile = donorProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donor profile required to respond."));

        if (profile.getParticipationStatus() != ParticipationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donor participation status must be ACTIVE to respond.");
        }

        if (!profile.isConsentToContact()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donor consent to contact must be enabled to respond.");
        }

        if (!isPotentialMatch(profile, request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Donor is not a potential match for this blood request.");
        }

        Optional<DonorResponse> existingOpt = donorResponseRepository.findByRequestIdAndDonorId(requestId, user.getId());
        DonorResponse savedResponse;

        if (existingOpt.isPresent()) {
            DonorResponse existing = existingOpt.get();
            if (existing.getResponseStatus() != ResponseStatus.WITHDRAWN) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already responded to this request.");
            }
            existing.setResponseStatus(ResponseStatus.INTERESTED);
            existing.setMessage(requestDto != null ? requestDto.getMessage() : null);
            savedResponse = donorResponseRepository.save(existing);
        } else {
            DonorResponse newResponse = new DonorResponse();
            newResponse.setRequest(request);
            newResponse.setDonor(user);
            newResponse.setResponseStatus(ResponseStatus.INTERESTED);
            newResponse.setMessage(requestDto != null ? requestDto.getMessage() : null);
            savedResponse = donorResponseRepository.save(newResponse);
        }

        createTimelineEvent(request, user, "DONOR_RESPONSE_CREATED", "Potential donor response received");

        return mapToResponse(savedResponse);
    }

    @Transactional(readOnly = true)
    public List<DonorResponseResponse> getResponsesForRequest(Long requestId, UserPrincipal principal) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + requestId));

        if (!request.getRequester().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedException("You are not authorized to view responses for this request.");
        }

        return donorResponseRepository.findByRequestId(requestId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DonorResponseResponse> getMyResponses(UserPrincipal principal) {
        User user = principal.getUser();
        if (user.getRole() != Role.DONOR) {
            throw new AccessDeniedException("Only donors can view their responses.");
        }

        return donorResponseRepository.findByDonorId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DonorResponseResponse getResponseById(Long responseId, UserPrincipal principal) {
        DonorResponse response = donorResponseRepository.findById(responseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found with ID: " + responseId));

        Long currentUserId = principal.getUser().getId();
        boolean isDonor = response.getDonor().getId().equals(currentUserId);
        boolean isRequester = response.getRequest().getRequester().getId().equals(currentUserId);

        if (!isDonor && !isRequester) {
            throw new AccessDeniedException("You are not authorized to view this response.");
        }

        return mapToResponse(response);
    }

    @Transactional
    public DonorResponseResponse updateResponseStatus(Long responseId, UserPrincipal principal, ResponseStatusUpdateRequest updateDto) {
        DonorResponse response = donorResponseRepository.findById(responseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found with ID: " + responseId));

        Long currentUserId = principal.getUser().getId();
        boolean isDonor = response.getDonor().getId().equals(currentUserId);
        boolean isRequester = response.getRequest().getRequester().getId().equals(currentUserId);

        if (!isDonor && !isRequester) {
            throw new AccessDeniedException("You are not authorized to modify this response.");
        }

        ResponseStatus oldStatus = response.getResponseStatus();
        ResponseStatus newStatus = updateDto.getResponseStatus();

        if (isDonor) {
            if (newStatus != ResponseStatus.WITHDRAWN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donors may only withdraw their response.");
            }
            if (oldStatus != ResponseStatus.INTERESTED && oldStatus != ResponseStatus.CONTACTED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot withdraw response from current state: " + oldStatus);
            }
        }

        if (isRequester) {
            if (oldStatus == ResponseStatus.INTERESTED && newStatus != ResponseStatus.CONTACTED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INTERESTED response can only be updated to CONTACTED by requester.");
            }
            if (oldStatus == ResponseStatus.CONTACTED && newStatus != ResponseStatus.CONFIRMED_BY_REQUESTER) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CONTACTED response can only be updated to CONFIRMED_BY_REQUESTER by requester.");
            }
            if (oldStatus == ResponseStatus.CONFIRMED_BY_REQUESTER || oldStatus == ResponseStatus.WITHDRAWN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update status from terminal state: " + oldStatus);
            }
        }

        response.setResponseStatus(newStatus);
        DonorResponse savedResponse = donorResponseRepository.save(response);

        createTimelineEvent(
                response.getRequest(),
                principal.getUser(),
                "DONOR_RESPONSE_STATUS_CHANGED",
                "Donor response status changed from " + oldStatus + " to " + newStatus
        );

        return mapToResponse(savedResponse);
    }

    private boolean isPotentialMatch(DonorProfile profile, BloodRequest request) {
        if (profile.getBloodGroup() != request.getBloodGroup()) {
            return false;
        }

        String hospitalLoc = request.getHospitalLocation() != null ? request.getHospitalLocation().trim().toLowerCase() : "";
        String preferredLocations = profile.getPreferredLocations();

        if (preferredLocations == null || preferredLocations.isBlank() || hospitalLoc.isBlank()) {
            return false;
        }

        String[] tokens = preferredLocations.split(",");
        for (String rawToken : tokens) {
            String token = rawToken.trim().toLowerCase();
            if (!token.isEmpty()) {
                if (hospitalLoc.contains(token) || token.contains(hospitalLoc) || token.equalsIgnoreCase(hospitalLoc)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createTimelineEvent(BloodRequest request, User actor, String eventType, String description) {
        RequestEvent event = new RequestEvent();
        event.setRequest(request);
        event.setActor(actor);
        event.setEventType(eventType);
        event.setDescription(description);
        requestEventRepository.save(event);
    }

    private DonorResponseResponse mapToResponse(DonorResponse response) {
        return new DonorResponseResponse(
                response.getId(),
                response.getRequest().getId(),
                response.getDonor().getId(),
                response.getDonor().getName(),
                response.getResponseStatus(),
                response.getMessage(),
                response.getCreatedAt(),
                response.getUpdatedAt()
        );
    }
}
