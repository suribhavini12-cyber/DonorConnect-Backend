package com.donorconnect.controller;

import com.donorconnect.dto.BloodRequestRequest;
import com.donorconnect.dto.BloodRequestResponse;
import com.donorconnect.model.BloodGroup;
import com.donorconnect.model.RequestStatus;
import com.donorconnect.model.Urgency;
import com.donorconnect.security.JwtAuthenticationFilter;
import com.donorconnect.security.JwtService;
import com.donorconnect.service.BloodRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BloodRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BloodRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BloodRequestService bloodRequestService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/requests returns 200 OK with requests list")
    void getAllRequests_Success() throws Exception {
        BloodRequestResponse resp = new BloodRequestResponse(
                10L, 1L, "Alice", BloodGroup.O_POSITIVE, "WHOLE_BLOOD", 2,
                "City Hospital", "Downtown", Urgency.HIGH, LocalDateTime.now().plusDays(2),
                "Urgent", RequestStatus.OPEN, LocalDateTime.now(), LocalDateTime.now()
        );

        when(bloodRequestService.getAllRequests()).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].bloodGroup").value("O_POSITIVE"));
    }

    @Test
    @DisplayName("POST /api/requests returns 201 Created")
    void createRequest_Success() throws Exception {
        BloodRequestRequest request = new BloodRequestRequest();
        request.setBloodGroup(BloodGroup.O_POSITIVE);
        request.setBloodComponent("WHOLE_BLOOD");
        request.setUnitsRequired(2);
        request.setHospitalName("City Hospital");
        request.setHospitalLocation("Downtown");
        request.setUrgency(Urgency.HIGH);
        request.setRequiredBy(LocalDateTime.now().plusDays(2));
        request.setDescription("Urgent blood needed for surgery");

        BloodRequestResponse resp = new BloodRequestResponse(
                11L, 1L, "Alice", BloodGroup.O_POSITIVE, "WHOLE_BLOOD", 2,
                "City Hospital", "Downtown", Urgency.HIGH, LocalDateTime.now().plusDays(2),
                "Urgent", RequestStatus.PENDING_REVIEW, LocalDateTime.now(), LocalDateTime.now()
        );

        when(bloodRequestService.createRequest(any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"));
    }
}
