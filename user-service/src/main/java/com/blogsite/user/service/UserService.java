package com.blogsite.user.service;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.UserRegistrationDTO;
import com.blogsite.common.exception.DuplicateResourceException;
import com.blogsite.common.exception.ResourceNotFoundException;
import com.blogsite.user.entity.User;
import com.blogsite.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service
 * Business logic for user management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BackupService backupService;
    
    /**
     * Register a new user
     * US_01: User Registration
     */
    @Transactional
    public ApiResponse<User> registerUser(UserRegistrationDTO registrationDTO) {
        log.info("Attempting to register user with email: {}", registrationDTO.getUserEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(registrationDTO.getUserEmail())) {
            log.error("User with email {} already exists", registrationDTO.getUserEmail());
            throw new DuplicateResourceException("User with email " + registrationDTO.getUserEmail() + " already exists");
        }
        
        // Create user entity
        User user = User.builder()
                .userName(registrationDTO.getUserName())
                .email(registrationDTO.getUserEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .isActive(true)
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());
        
        // Check if backup is needed
        checkAndTriggerBackup();
        
        // Remove password from response
        savedUser.setPassword(null);
        
        return ApiResponse.success(savedUser, "User registered successfully");
    }
    
    /**
     * Authenticate user and return user details
     */
    @Transactional(readOnly = true)
    public User authenticateUser(String email, String password) {
        log.info("Attempting to authenticate user: {}", email);
        
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found or inactive: " + email));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("Invalid password for user: {}", email);
            throw new ResourceNotFoundException("Invalid credentials");
        }
        
        log.info("User authenticated successfully: {}", email);
        return user;
    }
    
    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Check if backup is needed (when count crosses 10,000)
     */
    private void checkAndTriggerBackup() {
        long userCount = userRepository.countTotalUsers();
        if (userCount >= 10000 && userCount % 10000 == 0) {
            log.info("User count reached {}, triggering backup", userCount);
            backupService.triggerBackup();
        }
    }
}
