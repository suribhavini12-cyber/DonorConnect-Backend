package com.donorconnect.service;

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
import com.donorconnect.security.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String trimmedName = request.getName() != null ? request.getName().trim() : "";
        String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("An account with email '" + normalizedEmail + "' already exists.");
        }

        if (request.getRole() == Role.ADMIN) {
            throw new InvalidRoleException("Public registration as ADMIN is not allowed.");
        }

        if (request.getRole() != Role.DONOR && request.getRole() != Role.REQUESTER) {
            throw new InvalidRoleException("Role must be either DONOR or REQUESTER.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setName(trimmedName);
        user.setEmail(normalizedEmail);
        user.setPassword(encodedPassword);
        user.setRole(request.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                "User registered successfully"
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!user.isActive()) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateToken(userPrincipal, user.getRole().name());

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                "Login successful"
        );
    }
}
