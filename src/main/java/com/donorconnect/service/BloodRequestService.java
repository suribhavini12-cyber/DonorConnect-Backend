package com.donorconnect.service;

import com.donorconnect.dto.BloodRequestRequest;
import com.donorconnect.dto.BloodRequestResponse;
import com.donorconnect.dto.RequestEventResponse;
import com.donorconnect.exception.InvalidStatusTransitionException;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.RequestEvent;
import com.donorconnect.model.RequestStatus;
import com.donorconnect.model.User;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.RequestEventRepository;
import com.donorconnect.security.UserPrincipal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final RequestEventRepository requestEventRepository;

    public BloodRequestService(
            BloodRequestRepository bloodRequestRepository,
            RequestEventRepository requestEventRepository
    ) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.requestEventRepository = requestEventRepository;
    }

    @Transactional
    @CacheEvict(value = "openRequests", allEntries = true)
    public BloodRequestResponse createRequest(UserPrincipal principal, BloodRequestRequest requestDto) {
        User user = principal.getUser();

        BloodRequest request = new BloodRequest();
        request.setRequester(user);
        request.setBloodGroup(requestDto.getBloodGroup());
        request.setBloodComponent(requestDto.getBloodComponent());
        request.setUnitsRequired(requestDto.getUnitsRequired());
        request.setHospitalName(requestDto.getHospitalName());
        request.setHospitalLocation(requestDto.getHospitalLocation());
        request.setUrgency(requestDto.getUrgency());
        request.setRequiredBy(requestDto.getRequiredBy());
        request.setDescription(requestDto.getDescription());
        request.setStatus(RequestStatus.PENDING_REVIEW);

        BloodRequest savedRequest = bloodRequestRepository.save(request);

        createEvent(savedRequest, user, "REQUEST_CREATED", "Blood request created");

        return mapToResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "openRequests")
    public List<BloodRequestResponse> getAllRequests() {
        return bloodRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BloodRequestResponse> getMyRequests(UserPrincipal principal) {
        User user = principal.getUser();
        return bloodRequestRepository.findByRequester(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BloodRequestResponse getRequestById(UserPrincipal principal, Long id) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + id));

        return mapToResponse(request);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "openRequests", allEntries = true),
            @CacheEvict(value = "donorMatches", key = "#id")
    })
    public BloodRequestResponse updateRequest(UserPrincipal principal, Long id, BloodRequestRequest requestDto) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + id));

        validateOwnership(principal, request);

        if (request.getStatus() != RequestStatus.PENDING_REVIEW && request.getStatus() != RequestStatus.OPEN) {
            throw new InvalidStatusTransitionException("Cannot update request with status: " + request.getStatus());
        }

        request.setBloodGroup(requestDto.getBloodGroup());
        request.setBloodComponent(requestDto.getBloodComponent());
        request.setUnitsRequired(requestDto.getUnitsRequired());
        request.setHospitalName(requestDto.getHospitalName());
        request.setHospitalLocation(requestDto.getHospitalLocation());
        request.setUrgency(requestDto.getUrgency());
        request.setRequiredBy(requestDto.getRequiredBy());
        request.setDescription(requestDto.getDescription());

        BloodRequest savedRequest = bloodRequestRepository.save(request);

        createEvent(savedRequest, principal.getUser(), "REQUEST_UPDATED", "Blood request updated");

        return mapToResponse(savedRequest);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "openRequests", allEntries = true),
            @CacheEvict(value = "donorMatches", key = "#id")
    })
    public BloodRequestResponse cancelRequest(UserPrincipal principal, Long id) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + id));

        validateOwnership(principal, request);

        request.setStatus(RequestStatus.CANCELLED);
        BloodRequest savedRequest = bloodRequestRepository.save(request);

        createEvent(savedRequest, principal.getUser(), "REQUEST_CANCELLED", "Blood request cancelled");

        return mapToResponse(savedRequest);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "openRequests", allEntries = true),
            @CacheEvict(value = "donorMatches", key = "#id")
    })
    public BloodRequestResponse updateRequestStatus(UserPrincipal principal, Long id, RequestStatus newStatus) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + id));

        validateOwnership(principal, request);

        RequestStatus currentStatus = request.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        request.setStatus(newStatus);
        BloodRequest savedRequest = bloodRequestRepository.save(request);

        String eventDesc = "Status changed from " + currentStatus + " to " + newStatus;
        createEvent(savedRequest, principal.getUser(), "STATUS_CHANGED", eventDesc);

        return mapToResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<RequestEventResponse> getRequestTimeline(UserPrincipal principal, Long id) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + id));

        return requestEventRepository.findByRequestIdOrderByCreatedAtAsc(request.getId()).stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    private void validateOwnership(UserPrincipal principal, BloodRequest request) {
        if (!request.getRequester().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to modify this request.");
        }
    }

    private void validateStatusTransition(RequestStatus current, RequestStatus target) {
        if (current == target) {
            return;
        }

        boolean valid = false;

        switch (current) {
            case PENDING_REVIEW:
                valid = (target == RequestStatus.OPEN || target == RequestStatus.CANCELLED);
                break;
            case OPEN:
                valid = (target == RequestStatus.PARTIALLY_RESPONDED || target == RequestStatus.FULFILLED || target == RequestStatus.CANCELLED);
                break;
            case PARTIALLY_RESPONDED:
                valid = (target == RequestStatus.FULFILLED || target == RequestStatus.CANCELLED);
                break;
            default:
                valid = false;
        }

        if (!valid) {
            throw new InvalidStatusTransitionException("Cannot transition status from " + current + " to " + target);
        }
    }

    private void createEvent(BloodRequest request, User actor, String eventType, String description) {
        RequestEvent event = new RequestEvent();
        event.setRequest(request);
        event.setActor(actor);
        event.setEventType(eventType);
        event.setDescription(description);
        requestEventRepository.save(event);
    }

    private BloodRequestResponse mapToResponse(BloodRequest request) {
        return new BloodRequestResponse(
                request.getId(),
                request.getRequester().getId(),
                request.getRequester().getName(),
                request.getBloodGroup(),
                request.getBloodComponent(),
                request.getUnitsRequired(),
                request.getHospitalName(),
                request.getHospitalLocation(),
                request.getUrgency(),
                request.getRequiredBy(),
                request.getDescription(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    private RequestEventResponse mapToEventResponse(RequestEvent event) {
        return new RequestEventResponse(
                event.getId(),
                event.getRequest().getId(),
                event.getActor().getId(),
                event.getActor().getName(),
                event.getEventType(),
                event.getDescription(),
                event.getCreatedAt()
        );
    }
}
