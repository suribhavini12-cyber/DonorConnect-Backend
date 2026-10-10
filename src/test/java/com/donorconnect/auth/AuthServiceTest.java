package com.donorconnect.auth;

import com.donorconnect.dto.LoginRequest;
import com.donorconnect.dto.LoginResponse;
import com.donorconnect.dto.RegisterRequest;
import com.donorconnect.dto.RegisterResponse;
import com.donorconnect.exception.EmailAlreadyExistsException;
import com.donorconnect.exception.InvalidRoleException;
import com.donorconnect.model.Role;
import com.donorconnect.model.User;
import com.donorconnect.repository.UserRepository;
import com.donorconnect.security.JwtService;
import com.donorconnect.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest donorRegisterRequest;
    private RegisterRequest requesterRegisterRequest;
    private User mockDonorUser;

    @BeforeEach
    void setUp() {
        donorRegisterRequest = new RegisterRequest();
        donorRegisterRequest.setName("Alice Donor");
        donorRegisterRequest.setEmail("alice@example.com");
        donorRegisterRequest.setPassword("securePassword123");
        donorRegisterRequest.setRole(Role.DONOR);

        requesterRegisterRequest = new RegisterRequest();
        requesterRegisterRequest.setName("Bob Requester");
        requesterRegisterRequest.setEmail("bob@example.com");
        requesterRegisterRequest.setPassword("securePassword123");
        requesterRegisterRequest.setRole(Role.REQUESTER);

        mockDonorUser = new User();
        mockDonorUser.setId(1L);
        mockDonorUser.setName("Alice Donor");
        mockDonorUser.setEmail("alice@example.com");
        mockDonorUser.setPassword("$2a$10$hashedPasswordHere");
        mockDonorUser.setRole(Role.DONOR);
        mockDonorUser.setActive(true);
    }

    @Test
    @DisplayName("Successful registration for DONOR role")
    void registerDonor_Success() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("$2a$10$hashedPasswordHere");
        when(userRepository.save(any(User.class))).thenReturn(mockDonorUser);

        RegisterResponse response = authService.register(donorRegisterRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice Donor");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo(Role.DONOR);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Successful registration for REQUESTER role")
    void registerRequester_Success() {
        User mockRequesterUser = new User();
        mockRequesterUser.setId(2L);
        mockRequesterUser.setName("Bob Requester");
        mockRequesterUser.setEmail("bob@example.com");
        mockRequesterUser.setRole(Role.REQUESTER);
        mockRequesterUser.setActive(true);

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenReturn(mockRequesterUser);

        RegisterResponse response = authService.register(requesterRegisterRequest);

        assertThat(response.getRole()).isEqualTo(Role.REQUESTER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("ADMIN role registration is strictly forbidden")
    void registerAdmin_ThrowsException() {
        RegisterRequest adminReq = new RegisterRequest();
        adminReq.setName("Admin Attacker");
        adminReq.setEmail("admin@example.com");
        adminReq.setPassword("pass123");
        adminReq.setRole(Role.ADMIN);

        assertThatThrownBy(() -> authService.register(adminReq))
                .isInstanceOf(InvalidRoleException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Duplicate email registration throws EmailAlreadyExistsException")
    void registerDuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(donorRegisterRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Password is BCrypt hashed and never stored in plain text")
    void register_HashesPassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenReturn(mockDonorUser);

        authService.register(donorRegisterRequest);

        verify(passwordEncoder).encode("securePassword123");
    }

    @Test
    @DisplayName("Successful login returns valid JWT token and response DTO")
    void login_Success() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("alice@example.com");
        loginReq.setPassword("securePassword123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(mockDonorUser));
        when(passwordEncoder.matches("securePassword123", mockDonorUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), anyString())).thenReturn("mocked.jwt.token");

        LoginResponse response = authService.login(loginReq);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Login with incorrect password throws BadCredentialsException")
    void login_WrongPassword_ThrowsException() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("alice@example.com");
        loginReq.setPassword("wrongpass");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(mockDonorUser));
        when(passwordEncoder.matches("wrongpass", mockDonorUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Login with unknown email throws BadCredentialsException")
    void login_UnknownEmail_ThrowsException() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("unknown@example.com");
        loginReq.setPassword("any");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(BadCredentialsException.class);
    }
}
