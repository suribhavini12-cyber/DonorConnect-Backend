package com.donorconnect.controller;

import com.donorconnect.dto.LoginRequest;
import com.donorconnect.dto.LoginResponse;
import com.donorconnect.dto.RegisterRequest;
import com.donorconnect.dto.RegisterResponse;
import com.donorconnect.model.Role;
import com.donorconnect.security.JwtAuthenticationFilter;
import com.donorconnect.security.JwtService;
import com.donorconnect.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/auth/register returns 201 Created on valid input")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice Donor");
        request.setEmail("alice@example.com");
        request.setPassword("securePassword123");
        request.setRole(Role.DONOR);

        RegisterResponse response = new RegisterResponse(1L, "Alice Donor", "alice@example.com", Role.DONOR, "Registration successful");

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("DONOR"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 Bad Request on invalid email format")
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice Donor");
        request.setEmail("invalid-email-format");
        request.setPassword("securePassword123");
        request.setRole(Role.DONOR);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 OK with token")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("securePassword123");

        LoginResponse response = new LoginResponse("token.123", 1L, "Alice Donor", "alice@example.com", Role.DONOR, "Login successful");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.123"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }
}
