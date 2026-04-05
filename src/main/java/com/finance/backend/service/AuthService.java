package com.finance.backend.service;

import com.finance.backend.dto.request.LoginRequest;
import com.finance.backend.dto.request.UserCreateRequest;
import com.finance.backend.dto.response.AuthResponse;
import com.finance.backend.dto.response.SignupResponse;
import com.finance.backend.entity.User;
import com.finance.backend.enums.UserStatus;
import com.finance.backend.exception.ConflictException;
import com.finance.backend.exception.UnauthorizedException;
import com.finance.backend.repository.UserRepository;
import com.finance.backend.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Pattern to detect potential SQL injection or XSS attempts
    private static final Pattern MALICIOUS_PATTERN = Pattern.compile(
        ".*(<script|javascript:|onerror=|onload=|<iframe|SELECT.*FROM|INSERT.*INTO|DROP.*TABLE|--|;).*",
        Pattern.CASE_INSENSITIVE
    );

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String password = request.getPassword() != null ? request.getPassword() : "";

        // Detect and reject malicious input
        if (MALICIOUS_PATTERN.matcher(username).matches() || MALICIOUS_PATTERN.matcher(password).matches()) {
            log.warn("Potential security threat detected in login attempt for username: {}", username);
            // Use same error message to prevent information disclosure
            throw new UnauthorizedException("Invalid username or password");
        }

        // Find user (excluding soft-deleted users)
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElse(null);

        // Constant-time response for non-existent user
        if (user == null) {
            // Perform a dummy password check to prevent timing attacks
            passwordEncoder.matches(password, "$2a$12$dummyHashToPreventTimingAttack1234567890");
            log.info("Failed login attempt for non-existent user: {}", username);
            throw new UnauthorizedException("Invalid username or password");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.info("Failed login attempt for user: {} (incorrect password)", username);
            throw new UnauthorizedException("Invalid username or password");
        }

        // Check if user is active
        if (user.getStatus() == UserStatus.INACTIVE) {
            log.info("Login attempt for inactive user: {}", username);
            throw new UnauthorizedException("User account is inactive. Please contact an administrator.");
        }

        // Check if user is soft-deleted (extra safety check)
        if (user.getDeletedAt() != null) {
            log.info("Login attempt for deleted user: {}", username);
            throw new UnauthorizedException("Invalid username or password");
        }

        // Generate token with user status
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getStatus());

        log.info("Successful login for user: {}", username);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public SignupResponse register(UserCreateRequest request) {
        // Trim inputs
        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String fullName = request.getFullName() != null ? request.getFullName().trim() : null;

        // Check uniqueness excluding soft-deleted users
        if (userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            throw new ConflictException("Username already exists");
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new ConflictException("Email already exists");
        }

        // Validate password complexity
        validatePasswordComplexity(request.getPassword());

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(fullName)
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        try {
            user = userRepository.save(user);
            log.info("User registered successfully: {}", username);

            return SignupResponse.builder()
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation during registration for username: {}", username, e);
            throw new ConflictException("User with this username or email already exists");
        }
    }

    /**
     * Validates password complexity requirements
     */
    private void validatePasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            throw new ConflictException("Password must be at least 8 characters long");
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUppercase || !hasLowercase || !hasDigit) {
            throw new ConflictException(
                "Password must contain at least one uppercase letter, one lowercase letter, and one number"
            );
        }
    }
}
