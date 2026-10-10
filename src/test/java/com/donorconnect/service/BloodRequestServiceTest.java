package com.donorconnect.service;

import com.donorconnect.dto.BloodRequestRequest;
import com.donorconnect.dto.BloodRequestResponse;
import com.donorconnect.exception.InvalidStatusTransitionException;
import com.donorconnect.exception.RequestNotFoundException;
import com.donorconnect.model.*;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.repository.RequestEventRepository;
import com.donorconnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloodRequestServiceTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private RequestEventRepository requestEventRepository;

    @InjectMocks
    private BloodRequestService bloodRequestService;

    private User requesterUser;
    private User otherUser;
    private UserPrincipal requesterPrincipal;
    private UserPrincipal otherPrincipal;
    private BloodRequest mockRequest;

    @BeforeEach
    void setUp() {
        requesterUser = new User();
        requesterUser.setId(20L);
        requesterUser.setName("Requester Bob");
        requesterUser.setRole(Role.REQUESTER);

        otherUser = new User();
        otherUser.setId(99L);
        otherUser.setName("Other User");
        otherUser.setRole(Role.REQUESTER);

        requesterPrincipal = new UserPrincipal(requesterUser);
        otherPrincipal = new UserPrincipal(otherUser);

        mockRequest = new BloodRequest();
        mockRequest.setId(50L);
        mockRequest.setRequester(requesterUser);
        mockRequest.setBloodGroup(BloodGroup.O_POSITIVE);
        mockRequest.setBloodComponent("WHOLE_BLOOD");
        mockRequest.setUnitsRequired(2);
        mockRequest.setHospitalName("City Hospital");
        mockRequest.setHospitalLocation("Downtown");
        mockRequest.setUrgency(Urgency.HIGH);
        mockRequest.setStatus(RequestStatus.PENDING_REVIEW);
    }

    @Test
    @DisplayName("Create blood request successfully")
    void createRequest_Success() {
        BloodRequestRequest requestDto = new BloodRequestRequest();
        requestDto.setBloodGroup(BloodGroup.O_POSITIVE);
        requestDto.setBloodComponent("WHOLE_BLOOD");
        requestDto.setUnitsRequired(2);
        requestDto.setHospitalName("City Hospital");
        requestDto.setHospitalLocation("Downtown");
        requestDto.setUrgency(Urgency.HIGH);

        when(bloodRequestRepository.save(any(BloodRequest.class))).thenReturn(mockRequest);

        BloodRequestResponse response = bloodRequestService.createRequest(requesterPrincipal, requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(RequestStatus.PENDING_REVIEW);
        verify(requestEventRepository).save(any(RequestEvent.class));
    }

    @Test
    @DisplayName("Retrieve all requests")
    void getAllRequests_Success() {
        when(bloodRequestRepository.findAll()).thenReturn(List.of(mockRequest));

        List<BloodRequestResponse> list = bloodRequestService.getAllRequests();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Cancel request by owner successfully")
    void cancelRequest_Success() {
        when(bloodRequestRepository.findById(50L)).thenReturn(Optional.of(mockRequest));
        when(bloodRequestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequestResponse response = bloodRequestService.cancelRequest(requesterPrincipal, 50L);

        assertThat(response.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        verify(requestEventRepository).save(any(RequestEvent.class));
    }

    @Test
    @DisplayName("Non-owner updating or cancelling request throws AccessDeniedException")
    void cancelRequest_Unauthorized_ThrowsException() {
        when(bloodRequestRepository.findById(50L)).thenReturn(Optional.of(mockRequest));

        assertThatThrownBy(() -> bloodRequestService.cancelRequest(otherPrincipal, 50L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Valid status transition (PENDING_REVIEW -> OPEN)")
    void updateStatus_ValidTransition_Success() {
        when(bloodRequestRepository.findById(50L)).thenReturn(Optional.of(mockRequest));
        when(bloodRequestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));

        BloodRequestResponse response = bloodRequestService.updateRequestStatus(requesterPrincipal, 50L, RequestStatus.OPEN);

        assertThat(response.getStatus()).isEqualTo(RequestStatus.OPEN);
    }

    @Test
    @DisplayName("Invalid status transition (CANCELLED -> OPEN) throws InvalidStatusTransitionException")
    void updateStatus_InvalidTransition_ThrowsException() {
        mockRequest.setStatus(RequestStatus.CANCELLED);
        when(bloodRequestRepository.findById(50L)).thenReturn(Optional.of(mockRequest));

        assertThatThrownBy(() -> bloodRequestService.updateRequestStatus(requesterPrincipal, 50L, RequestStatus.OPEN))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("Request not found throws RequestNotFoundException")
    void getRequestById_NotFound_ThrowsException() {
        when(bloodRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bloodRequestService.getRequestById(requesterPrincipal, 999L))
                .isInstanceOf(RequestNotFoundException.class);
    }
}
