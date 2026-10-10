package com.donorconnect.service;

import com.donorconnect.dto.DonorProfileRequest;
import com.donorconnect.dto.DonorProfileResponse;
import com.donorconnect.exception.ProfileAlreadyExistsException;
import com.donorconnect.exception.ProfileNotFoundException;
import com.donorconnect.model.DonorProfile;
import com.donorconnect.model.ParticipationStatus;
import com.donorconnect.model.User;
import com.donorconnect.repository.DonorProfileRepository;
import com.donorconnect.security.UserPrincipal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;

    public DonorProfileService(DonorProfileRepository donorProfileRepository) {
        this.donorProfileRepository = donorProfileRepository;
    }

    @Transactional(readOnly = true)
    public DonorProfileResponse getProfile(UserPrincipal principal) {
        User user = principal.getUser();
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Donor profile not found."));

        return mapToResponse(profile);
    }

    @Transactional
    @CacheEvict(value = "donorMatches", allEntries = true)
    public DonorProfileResponse createProfile(UserPrincipal principal, DonorProfileRequest request) {
        User user = principal.getUser();

        if (donorProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new ProfileAlreadyExistsException("Donor profile already exists.");
        }

        DonorProfile profile = new DonorProfile();
        profile.setUser(user);
        profile.setBloodGroup(request.getBloodGroup());
        profile.setPreferredLocations(request.getPreferredLocations());
        profile.setParticipationStatus(request.getParticipationStatus());
        profile.setConsentToContact(Boolean.TRUE.equals(request.getConsentToContact()));

        DonorProfile saved = donorProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "donorMatches", allEntries = true)
    public DonorProfileResponse updateProfile(UserPrincipal principal, DonorProfileRequest request) {
        User user = principal.getUser();
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Donor profile not found."));

        profile.setBloodGroup(request.getBloodGroup());
        profile.setPreferredLocations(request.getPreferredLocations());
        profile.setParticipationStatus(request.getParticipationStatus());
        profile.setConsentToContact(Boolean.TRUE.equals(request.getConsentToContact()));

        DonorProfile saved = donorProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "donorMatches", allEntries = true)
    public DonorProfileResponse updateParticipationStatus(UserPrincipal principal, ParticipationStatus status) {
        User user = principal.getUser();
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Donor profile not found."));

        profile.setParticipationStatus(status);

        DonorProfile saved = donorProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    private DonorProfileResponse mapToResponse(DonorProfile profile) {
        return new DonorProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getBloodGroup(),
                profile.getPreferredLocations(),
                profile.getParticipationStatus(),
                profile.isConsentToContact(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
