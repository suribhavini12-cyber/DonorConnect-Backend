package com.donorconnect.controller;

import com.donorconnect.dto.LoginRequest;
import com.donorconnect.dto.LoginResponse;
import com.donorconnect.dto.RegisterRequest;
import com.donorconnect.dto.RegisterResponse;
import com.donorconnect.dto.UserDto;
import com.donorconnect.model.User;
import com.donorconnect.security.UserPrincipal;
import com.donorconnect.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public user registration, authentication login, and protected current-user profile endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Public endpoint to register a new user account with role DONOR or REQUESTER. Public ADMIN registration is strictly forbidden.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failure or invalid role requested"),
            @ApiResponse(responseCode = "409", description = "An account with the given email already exists")
    })
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Public endpoint to authenticate user credentials and return a signed JWT Bearer token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user details", description = "Protected endpoint returning current user details derived from the active JWT Bearer token.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or missing/expired JWT token")
    })
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = principal.getUser();
        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
        return ResponseEntity.ok(userDto);
    }
}
