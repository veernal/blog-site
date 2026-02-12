package com.blogsite.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LoginResponse DTO
 */
@DisplayName("LoginResponse DTO Tests")
class LoginResponseTest {
    
    @Test
    @DisplayName("LoginResponse - Builder Pattern")
    void testLoginResponse_Builder() {
        // When
        LoginResponse response = LoginResponse.builder()
                .userId("user-123")
                .userName("John Doe")
                .email("john@example.com")
                .token("jwt-token-xyz")
                .build();
        
        // Then
        assertNotNull(response);
        assertEquals("user-123", response.getUserId());
        assertEquals("John Doe", response.getUserName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("jwt-token-xyz", response.getToken());
    }
    
    @Test
    @DisplayName("LoginResponse - No Args Constructor")
    void testLoginResponse_NoArgsConstructor() {
        // When
        LoginResponse response = new LoginResponse();
        
        // Then
        assertNotNull(response);
        assertNull(response.getUserId());
        assertNull(response.getUserName());
        assertNull(response.getEmail());
        assertNull(response.getToken());
    }
    
    @Test
    @DisplayName("LoginResponse - All Args Constructor")
    void testLoginResponse_AllArgsConstructor() {
        // When
        LoginResponse response = new LoginResponse(
                "user-456",
                "Jane Smith",
                "jane@example.com",
                "token-abc"
        );
        
        // Then
        assertNotNull(response);
        assertEquals("user-456", response.getUserId());
        assertEquals("Jane Smith", response.getUserName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("token-abc", response.getToken());
    }
    
    @Test
    @DisplayName("LoginResponse - Getters and Setters")
    void testLoginResponse_GettersSetters() {
        // Given
        LoginResponse response = new LoginResponse();
        
        // When
        response.setUserId("user-789");
        response.setUserName("Bob Brown");
        response.setEmail("bob@example.com");
        response.setToken("token-def");
        
        // Then
        assertEquals("user-789", response.getUserId());
        assertEquals("Bob Brown", response.getUserName());
        assertEquals("bob@example.com", response.getEmail());
        assertEquals("token-def", response.getToken());
    }
    
    @Test
    @DisplayName("LoginResponse - Null Values")
    void testLoginResponse_NullValues() {
        // When
        LoginResponse response = LoginResponse.builder()
                .userId(null)
                .userName(null)
                .email(null)
                .token(null)
                .build();
        
        // Then
        assertNotNull(response);
        assertNull(response.getUserId());
        assertNull(response.getUserName());
        assertNull(response.getEmail());
        assertNull(response.getToken());
    }
}
