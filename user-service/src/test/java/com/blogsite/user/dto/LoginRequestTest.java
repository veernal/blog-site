package com.blogsite.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LoginRequest DTO validation
 */
@DisplayName("LoginRequest DTO Tests")
class LoginRequestTest {
    
    private static Validator validator;
    
    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Valid LoginRequest - No Violations")
    void testValidLoginRequest() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("Password123")
                .build();
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        
        // Then
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Invalid LoginRequest - Null Email")
    void testInvalidLoginRequest_NullEmail() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email(null)
                .password("Password123")
                .build();
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is mandatory")));
    }
    
    @Test
    @DisplayName("Invalid LoginRequest - Empty Email")
    void testInvalidLoginRequest_EmptyEmail() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("")
                .password("Password123")
                .build();
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        
        // Then
        assertFalse(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Invalid LoginRequest - Invalid Email Format")
    void testInvalidLoginRequest_InvalidEmailFormat() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("Password123")
                .build();
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email should be valid")));
    }
    
    @Test
    @DisplayName("Invalid LoginRequest - Null Password")
    void testInvalidLoginRequest_NullPassword() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password(null)
                .build();
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is mandatory")));
    }
    
    @Test
    @DisplayName("Invalid LoginRequest - Empty Password")
    void testInvalidLoginRequest_EmptyPassword() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("")
                .build();
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        
        // Then
        assertFalse(violations.isEmpty());
    }
    
    @Test
    @DisplayName("LoginRequest - Builder Pattern")
    void testLoginRequest_Builder() {
        // When
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("TestPass123")
                .build();
        
        // Then
        assertNotNull(loginRequest);
        assertEquals("test@example.com", loginRequest.getEmail());
        assertEquals("TestPass123", loginRequest.getPassword());
    }
    
    @Test
    @DisplayName("LoginRequest - Getters and Setters")
    void testLoginRequest_GettersSetters() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        
        // When
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("MyPassword");
        
        // Then
        assertEquals("user@example.com", loginRequest.getEmail());
        assertEquals("MyPassword", loginRequest.getPassword());
    }
    
    @Test
    @DisplayName("LoginRequest - All Args Constructor")
    void testLoginRequest_AllArgsConstructor() {
        // When
        LoginRequest loginRequest = new LoginRequest("admin@example.com", "AdminPass");
        
        // Then
        assertNotNull(loginRequest);
        assertEquals("admin@example.com", loginRequest.getEmail());
        assertEquals("AdminPass", loginRequest.getPassword());
    }
}
