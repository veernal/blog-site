package com.blogsite.user.controller;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.UserRegistrationDTO;
import com.blogsite.user.dto.LoginRequest;
import com.blogsite.user.dto.LoginResponse;
import com.blogsite.user.entity.User;
import com.blogsite.user.service.JwtService;
import com.blogsite.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * Handles user registration and authentication endpoints
 */
@RestController
@RequestMapping("/api/v1.0/blogsite/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user registration and authentication")
public class UserController {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    /**
     * US_01: User Registration
     * POST /api/v1.0/blogsite/user/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register as a new user with username, email and password")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("POST /api/v1.0/blogsite/user/register - Register user: {}", registrationDTO.getUserEmail());
        ApiResponse<User> response = userService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * User Login
     * POST /api/v1.0/blogsite/user/login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("POST /api/v1.0/blogsite/user/login - Login user: {}", loginRequest.getEmail());
        
        User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtService.generateToken(user.getUserId(), user.getEmail());
        
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .token(token)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }
}
