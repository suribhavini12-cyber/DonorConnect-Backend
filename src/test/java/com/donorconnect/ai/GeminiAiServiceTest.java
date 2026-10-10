package com.donorconnect.ai;

import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.BloodRequest;
import com.donorconnect.model.Role;
import com.donorconnect.model.User;
import com.donorconnect.repository.BloodRequestRepository;
import com.donorconnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiAiServiceTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeminiAiService geminiAiService;

    private User requesterUser;
    private User strangerUser;
    private UserPrincipal requesterPrincipal;
    private UserPrincipal strangerPrincipal;
    private BloodRequest mockRequest;

    @BeforeEach
    void setUp() {
        requesterUser = new User();
        requesterUser.setId(70L);
        requesterUser.setRole(Role.REQUESTER);
        requesterPrincipal = new UserPrincipal(requesterUser);

        strangerUser = new User();
        strangerUser.setId(88L);
        strangerUser.setRole(Role.DONOR);
        strangerPrincipal = new UserPrincipal(strangerUser);

        mockRequest = new BloodRequest();
        mockRequest.setId(700L);
        mockRequest.setRequester(requesterUser);
        mockRequest.setBloodGroup(BloodGroup.O_POSITIVE);
        mockRequest.setBloodComponent("WHOLE_BLOOD");
        mockRequest.setUnitsRequired(2);
        mockRequest.setHospitalName("St. Jude Hospital");
        mockRequest.setHospitalLocation("Downtown");
    }

    @Test
    @DisplayName("Request owner receives summary with fallback source when API key missing")
    void generateSummary_Fallback_Success() {
        when(bloodRequestRepository.findById(700L)).thenReturn(Optional.of(mockRequest));

        AiRequestSummaryResponse response = geminiAiService.generateSummary(requesterPrincipal, 700L);

        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).isEqualTo(700L);
        assertThat(response.getSource()).isEqualTo("FALLBACK");
        assertThat(response.getSummary()).contains("St. Jude Hospital");
    }

    @Test
    @DisplayName("Unrelated user requesting summary throws AccessDeniedException")
    void generateSummary_Unauthorized_ThrowsException() {
        when(bloodRequestRepository.findById(700L)).thenReturn(Optional.of(mockRequest));

        assertThatThrownBy(() -> geminiAiService.generateSummary(strangerPrincipal, 700L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Duplicate check returns advisory result with fallback source when API key missing")
    void checkForDuplicates_Fallback_Success() {
        when(bloodRequestRepository.findById(700L)).thenReturn(Optional.of(mockRequest));

        DuplicateCheckResponse response = geminiAiService.checkForDuplicates(requesterPrincipal, 700L);

        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).isEqualTo(700L);
        assertThat(response.getSource()).isEqualTo("FALLBACK");
    }
}
