package com.donorconnect.service;

import com.donorconnect.dto.DonorMatchResponse;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.*;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.DonorProfileRepository;
import com.donorconnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorMatchingServiceTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @InjectMocks
    private DonorMatchingService donorMatchingService;

    private User requesterUser;
    private UserPrincipal requesterPrincipal;
    private UserPrincipal strangerPrincipal;
    private BloodRequest openRequest;
    private DonorProfile matchingDonor;
    private DonorProfile nonMatchingLocationDonor;

    @BeforeEach
    void setUp() {
        requesterUser = new User();
        requesterUser.setId(30L);
        requesterUser.setRole(Role.REQUESTER);
        requesterPrincipal = new UserPrincipal(requesterUser);

        User stranger = new User();
        stranger.setId(88L);
        strangerPrincipal = new UserPrincipal(stranger);

        openRequest = new BloodRequest();
        openRequest.setId(300L);
        openRequest.setRequester(requesterUser);
        openRequest.setBloodGroup(BloodGroup.O_POSITIVE);
        openRequest.setHospitalLocation("Central Hospital Zone");
        openRequest.setStatus(RequestStatus.OPEN);

        User donorUser1 = new User();
        donorUser1.setId(101L);
        donorUser1.setName("Matched Donor");

        matchingDonor = new DonorProfile();
        matchingDonor.setId(501L);
        matchingDonor.setUser(donorUser1);
        matchingDonor.setBloodGroup(BloodGroup.O_POSITIVE);
        matchingDonor.setPreferredLocations("Central Hospital Zone, North");
        matchingDonor.setParticipationStatus(ParticipationStatus.ACTIVE);
        matchingDonor.setConsentToContact(true);

        User donorUser2 = new User();
        donorUser2.setId(102L);
        donorUser2.setName("Far Donor");

        nonMatchingLocationDonor = new DonorProfile();
        nonMatchingLocationDonor.setId(502L);
        nonMatchingLocationDonor.setUser(donorUser2);
        nonMatchingLocationDonor.setBloodGroup(BloodGroup.O_POSITIVE);
        nonMatchingLocationDonor.setPreferredLocations("West Outskirts");
        nonMatchingLocationDonor.setParticipationStatus(ParticipationStatus.ACTIVE);
        nonMatchingLocationDonor.setConsentToContact(true);
    }

    @Test
    @DisplayName("Matching blood group + matching location returns potential match")
    void findPotentialMatches_Success() {
        when(bloodRequestRepository.findById(300L)).thenReturn(Optional.of(openRequest));
        when(donorProfileRepository.findByBloodGroupAndParticipationStatusAndConsentToContactTrue(
                BloodGroup.O_POSITIVE, ParticipationStatus.ACTIVE
        )).thenReturn(List.of(matchingDonor, nonMatchingLocationDonor));

        List<DonorMatchResponse> matches = donorMatchingService.findPotentialMatches(requesterPrincipal, 300L);

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getDonorId()).isEqualTo(101L);
        assertThat(matches.get(0).getDonorName()).isEqualTo("Matched Donor");
        assertThat(matches.get(0).getMatchReason()).contains("Potential match");
    }

    @Test
    @DisplayName("Unrelated user calling matching throws AccessDeniedException")
    void findPotentialMatches_Unauthorized_ThrowsException() {
        when(bloodRequestRepository.findById(300L)).thenReturn(Optional.of(openRequest));

        assertThatThrownBy(() -> donorMatchingService.findPotentialMatches(strangerPrincipal, 300L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Cancelled or FULFILLED requests return empty matches")
    void findPotentialMatches_CancelledRequest_ReturnsEmpty() {
        openRequest.setStatus(RequestStatus.CANCELLED);
        when(bloodRequestRepository.findById(300L)).thenReturn(Optional.of(openRequest));

        List<DonorMatchResponse> matches = donorMatchingService.findPotentialMatches(requesterPrincipal, 300L);

        assertThat(matches).isEmpty();
        verify(donorProfileRepository, never()).findByBloodGroupAndParticipationStatusAndConsentToContactTrue(any(), any());
    }

    @Test
    @DisplayName("Non-existent request ID throws RequestNotFoundException")
    void findPotentialMatches_NotFound_ThrowsException() {
        when(bloodRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorMatchingService.findPotentialMatches(requesterPrincipal, 999L))
                .isInstanceOf(RequestNotFoundException.class);
    }
}
