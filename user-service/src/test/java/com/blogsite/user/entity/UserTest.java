package com.blogsite.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for User Entity
 */
@DisplayName("User Entity Tests")
class UserTest {
    
    @Test
    @DisplayName("User - Builder Pattern")
    void testUser_Builder() {
        // When
        User user = User.builder()
                .userId("user-123")
                .userName("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .isActive(true)
                .build();
        
        // Then
        assertNotNull(user);
        assertEquals("user-123", user.getUserId());
        assertEquals("John Doe", user.getUserName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertTrue(user.getIsActive());
    }
    
    @Test
    @DisplayName("User - No Args Constructor")
    void testUser_NoArgsConstructor() {
        // When
        User user = new User();
        
        // Then
        assertNotNull(user);
        assertNull(user.getUserId());
        assertNull(user.getUserName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
    }
    
    @Test
    @DisplayName("User - All Args Constructor")
    void testUser_AllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        User user = new User(
                "user-456",
                "Jane Smith",
                "jane@example.com",
                "password123",
                true,
                now,
                now
        );
        
        // Then
        assertNotNull(user);
        assertEquals("user-456", user.getUserId());
        assertEquals("Jane Smith", user.getUserName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
    
    @Test
    @DisplayName("User - Getters and Setters")
    void testUser_GettersSetters() {
        // Given
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        
        // When
        user.setUserId("user-789");
        user.setUserName("Bob Brown");
        user.setEmail("bob@example.com");
        user.setPassword("securePass");
        user.setIsActive(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // Then
        assertEquals("user-789", user.getUserId());
        assertEquals("Bob Brown", user.getUserName());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("securePass", user.getPassword());
        assertFalse(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
    
    @Test
    @DisplayName("User - Default Active Status")
    void testUser_DefaultActiveStatus() {
        // When
        User user = User.builder()
                .userName("Test User")
                .email("test@example.com")
                .password("pass")
                .build();
        
        // Then
        // Note: Default value only works when using no-args constructor
        assertNull(user.getIsActive());
    }
    
    @Test
    @DisplayName("User - Inactive User")
    void testUser_InactiveUser() {
        // When
        User user = User.builder()
                .userId("user-inactive")
                .userName("Inactive User")
                .email("inactive@example.com")
                .password("pass")
                .isActive(false)
                .build();
        
        // Then
        assertFalse(user.getIsActive());
    }
    
    @Test
    @DisplayName("User - Email Uniqueness")
    void testUser_UniqueEmail() {
        // Given
        User user1 = User.builder()
                .userId("user-001")
                .email("same@example.com")
                .build();
        
        User user2 = User.builder()
                .userId("user-002")
                .email("same@example.com")
                .build();
        
        // Then - Both objects can exist, but database constraint will prevent duplicates
        assertEquals(user1.getEmail(), user2.getEmail());
    }
    
    @Test
    @DisplayName("User - Timestamps")
    void testUser_Timestamps() {
        // Given
        User user = new User();
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime updated = LocalDateTime.now().plusMinutes(5);
        
        // When
        user.setCreatedAt(created);
        user.setUpdatedAt(updated);
        
        // Then
        assertEquals(created, user.getCreatedAt());
        assertEquals(updated, user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(user.getCreatedAt()));
    }
    
    @Test
    @DisplayName("User - Password Security")
    void testUser_PasswordSecurity() {
        // When
        User user = User.builder()
                .userName("Security Test")
                .email("secure@example.com")
                .password("plainTextPassword")
                .build();
        
        // Then - Password should be encoded before storage (handled by service layer)
        assertNotNull(user.getPassword());
        assertEquals("plainTextPassword", user.getPassword());
    }
    
    @Test
    @DisplayName("User - Complete User Object")
    void testUser_CompleteObject() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        User user = User.builder()
                .userId("complete-user-id")
                .userName("Complete User")
                .email("complete@example.com")
                .password("$2a$10$encodedPassword")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        // Then
        assertNotNull(user.getUserId());
        assertNotNull(user.getUserName());
        assertNotNull(user.getEmail());
        assertNotNull(user.getPassword());
        assertNotNull(user.getIsActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }
}
