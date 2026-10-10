package com.donorconnect.service;

import com.donorconnect.dto.DonorMatchResponse;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.DonorProfile;
import com.donorconnect.model.ParticipationStatus;
import com.donorconnect.model.RequestStatus;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.DonorProfileRepository;
import com.donorconnect.security.UserPrincipal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonorMatchingService {

    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;

    public DonorMatchingService(
            BloodRequestRepository bloodRequestRepository,
            DonorProfileRepository donorProfileRepository
    ) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.donorProfileRepository = donorProfileRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "donorMatches", key = "#requestId")
    public List<DonorMatchResponse> findPotentialMatches(UserPrincipal principal, Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Blood request not found with ID: " + requestId));

        if (!request.getRequester().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedException("You are not authorized to view matches for this request.");
        }

        RequestStatus status = request.getStatus();
        if (status == RequestStatus.CANCELLED || status == RequestStatus.CLOSED || status == RequestStatus.FULFILLED) {
            return Collections.emptyList();
        }

        List<DonorProfile> candidates = donorProfileRepository
                .findByBloodGroupAndParticipationStatusAndConsentToContactTrue(
                        request.getBloodGroup(),
                        ParticipationStatus.ACTIVE
                );

        String hospitalLoc = request.getHospitalLocation() != null ? request.getHospitalLocation().trim().toLowerCase() : "";

        return candidates.stream()
                .filter(profile -> matchesLocation(profile.getPreferredLocations(), hospitalLoc))
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());
    }

    private boolean matchesLocation(String preferredLocations, String hospitalLoc) {
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

    private DonorMatchResponse mapToMatchResponse(DonorProfile profile) {
        return new DonorMatchResponse(
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getBloodGroup(),
                profile.getPreferredLocations(),
                profile.getParticipationStatus(),
                profile.isConsentToContact(),
                "Potential match because the blood group matches and the request location is within the donor's selected preferred area."
        );
    }
}
