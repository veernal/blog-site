package com.blogsite.user.controller;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.UserRegistrationDTO;
import com.blogsite.common.exception.DuplicateResourceException;
import com.blogsite.common.exception.ResourceNotFoundException;
import com.blogsite.user.dto.LoginRequest;
import com.blogsite.user.entity.User;
import com.blogsite.user.service.JwtService;
import com.blogsite.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 * Uses @WebMvcTest to test only the web layer without loading the full application context
 */
@WebMvcTest(UserController.class)
@WithMockUser
@DisplayName("User Controller Tests")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private JwtService jwtService;
    
    private UserRegistrationDTO validRegistrationDTO;
    private LoginRequest validLoginRequest;
    private User validUser;
    
    @BeforeEach
    void setUp() {
        validRegistrationDTO = UserRegistrationDTO.builder()
                .userName("John Doe")
                .userEmail("john.doe@example.com")
                .password("Password123")
                .build();
        
        validLoginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("Password123")
                .build();
        
        validUser = User.builder()
                .userId("user-123")
                .userName("John Doe")
                .email("john.doe@example.com")
                .isActive(true)
                .build();
    }
    
    @Test
    @DisplayName("POST /register - Success")
    void testRegisterUser_Success() throws Exception {
        // Given
        ApiResponse<User> apiResponse = ApiResponse.success(validUser, "User registered successfully");
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(apiResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.userId").value("user-123"))
                .andExpect(jsonPath("$.data.userName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }
    
    @Test
    @DisplayName("POST /register - Duplicate Email")
    void testRegisterUser_DuplicateEmail() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new DuplicateResourceException("User with email already exists"));
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isConflict());
    }
    
    @Test
    @DisplayName("POST /register - Invalid Email Format")
    void testRegisterUser_InvalidEmail() throws Exception {
        // Given
        validRegistrationDTO.setUserEmail("invalid-email");
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /register - Missing Required Fields")
    void testRegisterUser_MissingFields() throws Exception {
        // Given
        UserRegistrationDTO invalidDTO = UserRegistrationDTO.builder().build();
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /login - Success")
    void testLogin_Success() throws Exception {
        // Given
        when(userService.authenticateUser(anyString(), anyString())).thenReturn(validUser);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("jwt-token-12345");
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.userId").value("user-123"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-12345"));
    }
    
    @Test
    @DisplayName("POST /login - Invalid Credentials")
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        when(userService.authenticateUser(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Invalid credentials"));
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("POST /login - Missing Email")
    void testLogin_MissingEmail() throws Exception {
        // Given
        validLoginRequest.setEmail(null);
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /login - Missing Password")
    void testLogin_MissingPassword() throws Exception {
        // Given
        validLoginRequest.setPassword(null);
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /register - Empty Username")
    void testRegisterUser_EmptyUsername() throws Exception {
        // Given
        validRegistrationDTO.setUserName("");
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /register - Weak Password")
    void testRegisterUser_WeakPassword() throws Exception {
        // Given
        validRegistrationDTO.setPassword("123");
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest());
    }
}
