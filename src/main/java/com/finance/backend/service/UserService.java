package com.finance.backend.service;

import com.finance.backend.dto.request.UserCreateRequest;
import com.finance.backend.dto.request.UserUpdateRequest;
import com.finance.backend.dto.response.UserResponse;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.enums.UserStatus;
import com.finance.backend.exception.ConflictException;
import com.finance.backend.exception.ForbiddenException;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
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
            log.info("User created successfully: {}", username);
            return mapToResponse(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating user: {}", username, e);
            throw new ConflictException("User with this username or email already exists");
        }
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request, Long currentUserId) {
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Prevent users from modifying their own role
        if (id.equals(currentUserId) && request.getRole() != null && !request.getRole().equals(user.getRole())) {
            throw new ForbiddenException("Cannot modify your own role");
        }

        // Check if changing role from ADMIN and if this is the last admin
        if (request.getRole() != null && user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
            validateNotLastAdmin(id);
        }

        // Update email with uniqueness check
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            String email = request.getEmail().trim();
            if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(email);
        }

        // Update other fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getStatus() != null) {
            // Check if deactivating the last admin
            if (request.getStatus() == UserStatus.INACTIVE && user.getRole() == Role.ADMIN 
                && user.getStatus() == UserStatus.ACTIVE) {
                validateNotLastAdmin(id);
            }
            user.setStatus(request.getStatus());
        }

        try {
            user = userRepository.save(user);
            log.info("User updated successfully: {}", user.getUsername());
            return mapToResponse(user);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while updating user: {}", id);
            throw new ConflictException("User was modified by another transaction. Please refresh and try again.");
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating user: {}", id, e);
            throw new ConflictException("Email already exists");
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Integer limit, Integer offset) {
        // Default values
        int finalLimit = limit != null ? Math.min(limit, 1000) : 100; // Max 1000
        int finalOffset = offset != null ? Math.max(offset, 0) : 0;
        
        List<User> users = userRepository.findByDeletedAtIsNull();
        
        // Apply offset and limit
        return users.stream()
                .skip(finalOffset)
                .limit(finalLimit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Keep backward compatibility
    public List<UserResponse> getAllUsers() {
        return getAllUsers(null, null);
    }

    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if this is the last active admin
        if (user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE) {
            validateNotLastAdmin(id);
        }

        user.setStatus(UserStatus.INACTIVE);
        
        try {
            user = userRepository.save(user);
            log.info("User deactivated successfully: {}", user.getUsername());
            return mapToResponse(user);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while deactivating user: {}", id);
            throw new ConflictException("User was modified by another transaction. Please refresh and try again.");
        }
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setStatus(UserStatus.ACTIVE);
        
        try {
            user = userRepository.save(user);
            log.info("User activated successfully: {}", user.getUsername());
            return mapToResponse(user);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while activating user: {}", id);
            throw new ConflictException("User was modified by another transaction. Please refresh and try again.");
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if this is the last active admin
        if (user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE) {
            validateNotLastAdmin(id);
        }

        user.setDeletedAt(LocalDateTime.now());
        
        try {
            userRepository.save(user);
            log.info("User soft-deleted successfully: {}", user.getUsername());
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while deleting user: {}", id);
            throw new ConflictException("User was modified by another transaction. Please refresh and try again.");
        }
    }

    /**
     * Validates that the user is not the last active admin
     */
    private void validateNotLastAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE) {
            long activeAdminCount = userRepository.countActiveUsersByRole(Role.ADMIN);
            if (activeAdminCount <= 1) {
                throw new ForbiddenException(
                    "Cannot delete or deactivate the last active admin. " +
                    "Please create another admin before performing this operation."
                );
            }
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

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
