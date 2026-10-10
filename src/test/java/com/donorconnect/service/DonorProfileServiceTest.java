package com.donorconnect.service;

import com.donorconnect.dto.DonorProfileRequest;
import com.donorconnect.dto.DonorProfileResponse;
import com.donorconnect.exception.ProfileAlreadyExistsException;
import com.donorconnect.exception.ProfileNotFoundException;
import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.DonorProfile;
import com.donorconnect.model.ParticipationStatus;
import com.donorconnect.model.Role;
import com.donorconnect.model.User;
import com.donorconnect.repository.DonorProfileRepository;
import com.donorconnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorProfileServiceTest {

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @InjectMocks
    private DonorProfileService donorProfileService;

    private User donorUser;
    private UserPrincipal principal;
    private DonorProfile mockProfile;

    @BeforeEach
    void setUp() {
        donorUser = new User();
        donorUser.setId(10L);
        donorUser.setName("Donor John");
        donorUser.setEmail("john@example.com");
        donorUser.setRole(Role.DONOR);

        principal = new UserPrincipal(donorUser);

        mockProfile = new DonorProfile();
        mockProfile.setId(100L);
        mockProfile.setUser(donorUser);
        mockProfile.setBloodGroup(BloodGroup.O_POSITIVE);
        mockProfile.setPreferredLocations("Central Hospital");
        mockProfile.setParticipationStatus(ParticipationStatus.ACTIVE);
        mockProfile.setConsentToContact(true);
    }

    @Test
    @DisplayName("Create donor profile successfully")
    void createProfile_Success() {
        DonorProfileRequest request = new DonorProfileRequest();
        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setPreferredLocations("Central Hospital");
        request.setParticipationStatus(ParticipationStatus.ACTIVE);
        request.setConsentToContact(true);

        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(donorProfileRepository.save(any(DonorProfile.class))).thenReturn(mockProfile);

        DonorProfileResponse response = donorProfileService.createProfile(principal, request);

        assertThat(response).isNotNull();
        assertThat(response.getBloodGroup()).isEqualTo(BloodGroup.O_POSITIVE);
        verify(donorProfileRepository).save(any(DonorProfile.class));
    }

    @Test
    @DisplayName("Duplicate donor profile creation throws ProfileAlreadyExistsException")
    void createProfile_Duplicate_ThrowsException() {
        DonorProfileRequest request = new DonorProfileRequest();
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(mockProfile));

        assertThatThrownBy(() -> donorProfileService.createProfile(principal, request))
                .isInstanceOf(ProfileAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Get donor profile successfully")
    void getProfile_Success() {
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(mockProfile));

        DonorProfileResponse response = donorProfileService.getProfile(principal);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Get profile throws ProfileNotFoundException when non-existent")
    void getProfile_NotFound_ThrowsException() {
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorProfileService.getProfile(principal))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    @DisplayName("Update donor profile successfully")
    void updateProfile_Success() {
        DonorProfileRequest updateReq = new DonorProfileRequest();
        updateReq.setBloodGroup(BloodGroup.A_NEGATIVE);
        updateReq.setPreferredLocations("North Hospital");
        updateReq.setParticipationStatus(ParticipationStatus.ACTIVE);
        updateReq.setConsentToContact(false);

        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(mockProfile));
        when(donorProfileRepository.save(any(DonorProfile.class))).thenAnswer(i -> i.getArgument(0));

        DonorProfileResponse response = donorProfileService.updateProfile(principal, updateReq);

        assertThat(response.getBloodGroup()).isEqualTo(BloodGroup.A_NEGATIVE);
        assertThat(response.getPreferredLocations()).isEqualTo("North Hospital");
        assertThat(response.isConsentToContact()).isFalse();
    }

    @Test
    @DisplayName("Update participation status (ACTIVE -> PAUSED)")
    void updateParticipationStatus_Success() {
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(mockProfile));
        when(donorProfileRepository.save(any(DonorProfile.class))).thenAnswer(i -> i.getArgument(0));

        DonorProfileResponse response = donorProfileService.updateParticipationStatus(principal, ParticipationStatus.PAUSED);

        assertThat(response.getParticipationStatus()).isEqualTo(ParticipationStatus.PAUSED);
    }
}
