package com.donorconnect.service;

import com.donorconnect.dto.DonorResponseRequest;
import com.donorconnect.dto.DonorResponseResponse;
import com.donorconnect.dto.ResponseStatusUpdateRequest;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.*;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.DonorProfileRepository;
import com.donorconnect.repository.DonorResponseRepository;
import com.donorconnect.repository.RequestEventRepository;
import com.donorconnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorResponseServiceTest {

    @Mock
    private DonorResponseRepository donorResponseRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private RequestEventRepository requestEventRepository;

    @InjectMocks
    private DonorResponseService donorResponseService;

    private User donorUser;
    private User requesterUser;
    private UserPrincipal donorPrincipal;
    private UserPrincipal requesterPrincipal;
    private BloodRequest openRequest;
    private DonorProfile donorProfile;
    private DonorResponse existingResponse;

    @BeforeEach
    void setUp() {
        donorUser = new User();
        donorUser.setId(40L);
        donorUser.setName("Donor Dan");
        donorUser.setRole(Role.DONOR);
        donorPrincipal = new UserPrincipal(donorUser);

        requesterUser = new User();
        requesterUser.setId(41L);
        requesterUser.setName("Requester Rachel");
        requesterUser.setRole(Role.REQUESTER);
        requesterPrincipal = new UserPrincipal(requesterUser);

        openRequest = new BloodRequest();
        openRequest.setId(400L);
        openRequest.setRequester(requesterUser);
        openRequest.setBloodGroup(BloodGroup.O_POSITIVE);
        openRequest.setHospitalLocation("Downtown Hospital");
        openRequest.setStatus(RequestStatus.OPEN);

        donorProfile = new DonorProfile();
        donorProfile.setId(600L);
        donorProfile.setUser(donorUser);
        donorProfile.setBloodGroup(BloodGroup.O_POSITIVE);
        donorProfile.setPreferredLocations("Downtown Hospital");
        donorProfile.setParticipationStatus(ParticipationStatus.ACTIVE);
        donorProfile.setConsentToContact(true);

        existingResponse = new DonorResponse();
        existingResponse.setId(700L);
        existingResponse.setRequest(openRequest);
        existingResponse.setDonor(donorUser);
        existingResponse.setResponseStatus(ResponseStatus.INTERESTED);
        existingResponse.setMessage("I can donate tomorrow morning.");
    }

    @Test
    @DisplayName("Eligible donor responds INTERESTED successfully")
    void respondToRequest_Success() {
        DonorResponseRequest req = new DonorResponseRequest();
        req.setMessage("I can donate.");

        when(bloodRequestRepository.findById(400L)).thenReturn(Optional.of(openRequest));
        when(donorProfileRepository.findByUser(donorUser)).thenReturn(Optional.of(donorProfile));
        when(donorResponseRepository.findByRequestIdAndDonorId(400L, 40L)).thenReturn(Optional.empty());
        when(donorResponseRepository.save(any(DonorResponse.class))).thenAnswer(i -> {
            DonorResponse r = i.getArgument(0);
            r.setId(701L);
            return r;
        });

        DonorResponseResponse response = donorResponseService.createResponse(400L, donorPrincipal, req);

        assertThat(response).isNotNull();
        assertThat(response.getResponseStatus()).isEqualTo(ResponseStatus.INTERESTED);
        verify(requestEventRepository).save(any());
    }

    @Test
    @DisplayName("Duplicate active response throws ResponseStatusException CONFLICT")
    void respondToRequest_Duplicate_ThrowsException() {
        DonorResponseRequest req = new DonorResponseRequest();

        when(bloodRequestRepository.findById(400L)).thenReturn(Optional.of(openRequest));
        when(donorProfileRepository.findByUser(donorUser)).thenReturn(Optional.of(donorProfile));
        when(donorResponseRepository.findByRequestIdAndDonorId(400L, 40L)).thenReturn(Optional.of(existingResponse));

        assertThatThrownBy(() -> donorResponseService.createResponse(400L, donorPrincipal, req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("Donor can withdraw their response")
    void withdrawResponse_Success() {
        ResponseStatusUpdateRequest updateDto = new ResponseStatusUpdateRequest();
        updateDto.setResponseStatus(ResponseStatus.WITHDRAWN);

        when(donorResponseRepository.findById(700L)).thenReturn(Optional.of(existingResponse));
        when(donorResponseRepository.save(any(DonorResponse.class))).thenAnswer(i -> i.getArgument(0));

        DonorResponseResponse response = donorResponseService.updateResponseStatus(700L, donorPrincipal, updateDto);

        assertThat(response.getResponseStatus()).isEqualTo(ResponseStatus.WITHDRAWN);
        verify(requestEventRepository).save(any());
    }

    @Test
    @DisplayName("Requester updates response status to CONTACTED")
    void updateResponseStatus_ByRequester_Success() {
        ResponseStatusUpdateRequest updateDto = new ResponseStatusUpdateRequest();
        updateDto.setResponseStatus(ResponseStatus.CONTACTED);

        when(donorResponseRepository.findById(700L)).thenReturn(Optional.of(existingResponse));
        when(donorResponseRepository.save(any(DonorResponse.class))).thenAnswer(i -> i.getArgument(0));

        DonorResponseResponse response = donorResponseService.updateResponseStatus(700L, requesterPrincipal, updateDto);

        assertThat(response.getResponseStatus()).isEqualTo(ResponseStatus.CONTACTED);
    }

    @Test
    @DisplayName("Non-existent response ID throws ResponseStatusException")
    void withdrawResponse_NotFound_ThrowsException() {
        ResponseStatusUpdateRequest updateDto = new ResponseStatusUpdateRequest();
        updateDto.setResponseStatus(ResponseStatus.WITHDRAWN);

        when(donorResponseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorResponseService.updateResponseStatus(999L, donorPrincipal, updateDto))
                .isInstanceOf(ResponseStatusException.class);
    }
}
