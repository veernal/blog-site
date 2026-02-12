package com.blogsite.user.service;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.UserRegistrationDTO;
import com.blogsite.common.exception.DuplicateResourceException;
import com.blogsite.common.exception.ResourceNotFoundException;
import com.blogsite.user.entity.User;
import com.blogsite.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Tests both positive and negative scenarios
 * Target: 80%+ code coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private BackupService backupService;
    
    @InjectMocks
    private UserService userService;
    
    private UserRegistrationDTO validRegistrationDTO;
    private User validUser;
    
    @BeforeEach
    void setUp() {
        validRegistrationDTO = UserRegistrationDTO.builder()
                .userName("John Doe")
                .userEmail("john.doe@example.com")
                .password("Password123")
                .build();
        
        validUser = User.builder()
                .userId("user-123")
                .userName("John Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .isActive(true)
                .build();
    }
    
    @Test
    @DisplayName("Register User - Success Scenario")
    void testRegisterUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userRepository.countTotalUsers()).thenReturn(100L);
        
        // When
        ApiResponse<User> response = userService.registerUser(validRegistrationDTO);
        
        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("John Doe", response.getData().getUserName());
        assertEquals("john.doe@example.com", response.getData().getEmail());
        assertNull(response.getData().getPassword()); // Password should be removed
        
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
        verify(userRepository).countTotalUsers();
    }
    
    @Test
    @DisplayName("Register User - Duplicate Email")
    void testRegisterUser_DuplicateEmail() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.registerUser(validRegistrationDTO)
        );
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Register User - Triggers Backup at Threshold")
    void testRegisterUser_TriggersBackup() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userRepository.countTotalUsers()).thenReturn(10000L); // At threshold
        
        // When
        userService.registerUser(validRegistrationDTO);
        
        // Then
        verify(backupService).triggerBackup();
    }
    
    @Test
    @DisplayName("Register User - Does Not Trigger Backup Below Threshold")
    void testRegisterUser_NoBackupBelowThreshold() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userRepository.countTotalUsers()).thenReturn(9999L); // Below threshold
        
        // When
        userService.registerUser(validRegistrationDTO);
        
        // Then
        verify(backupService, never()).triggerBackup();
    }
    
    @Test
    @DisplayName("Authenticate User - Success Scenario")
    void testAuthenticateUser_Success() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        
        // When
        User authenticatedUser = userService.authenticateUser("john.doe@example.com", "Password123");
        
        // Then
        assertNotNull(authenticatedUser);
        assertEquals("user-123", authenticatedUser.getUserId());
        assertEquals("john.doe@example.com", authenticatedUser.getEmail());
        
        verify(userRepository).findActiveUserByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("Password123", "encodedPassword");
    }
    
    @Test
    @DisplayName("Authenticate User - User Not Found")
    void testAuthenticateUser_UserNotFound() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.authenticateUser("nonexistent@example.com", "Password123")
        );
        
        assertTrue(exception.getMessage().contains("User not found or inactive"));
        verify(userRepository).findActiveUserByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Authenticate User - Invalid Password")
    void testAuthenticateUser_InvalidPassword() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.authenticateUser("john.doe@example.com", "WrongPassword")
        );
        
        assertTrue(exception.getMessage().contains("Invalid credentials"));
        verify(userRepository).findActiveUserByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("WrongPassword", "encodedPassword");
    }
    
    @Test
    @DisplayName("Register User - Null Input")
    void testRegisterUser_NullInput() {
        // When & Then
        assertThrows(
                NullPointerException.class,
                () -> userService.registerUser(null)
        );
    }
    
    @Test
    @DisplayName("Authenticate User - Null Email")
    void testAuthenticateUser_NullEmail() {
        // When & Then
        assertThrows(
                Exception.class,
                () -> userService.authenticateUser(null, "Password123")
        );
    }
    
    @Test
    @DisplayName("Authenticate User - Null Password")
    void testAuthenticateUser_NullPassword() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.of(validUser));
        
        // When & Then
        assertThrows(
                Exception.class,
                () -> userService.authenticateUser("john.doe@example.com", null)
        );
    }
    
    @Test
    @DisplayName("Find User By Email - Success")
    void testFindByEmail_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(validUser));
        
        // When
        User foundUser = userService.findByEmail("john.doe@example.com");
        
        // Then
        assertNotNull(foundUser);
        assertEquals("john.doe@example.com", foundUser.getEmail());
        assertEquals("user-123", foundUser.getUserId());
        
        verify(userRepository).findByEmail("john.doe@example.com");
    }
    
    @Test
    @DisplayName("Find User By Email - User Not Found")
    void testFindByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.findByEmail("nonexistent@example.com")
        );
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findByEmail("nonexistent@example.com");
    }
    
    @Test
    @DisplayName("Register User - Triggers Backup at 20000")
    void testRegisterUser_TriggersBackupAt20000() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userRepository.countTotalUsers()).thenReturn(20000L);
        
        // When
        userService.registerUser(validRegistrationDTO);
        
        // Then
        verify(backupService).triggerBackup();
    }
    
    @Test
    @DisplayName("Register User - Does Not Trigger Backup at 10001")
    void testRegisterUser_NoBackupAt10001() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userRepository.countTotalUsers()).thenReturn(10001L);
        
        // When
        userService.registerUser(validRegistrationDTO);
        
        // Then
        verify(backupService, never()).triggerBackup();
    }
    
    @Test
    @DisplayName("Register User - Encrypts Password")
    void testRegisterUser_PasswordEncryption() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("super-encrypted-password");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userRepository.countTotalUsers()).thenReturn(100L);
        
        // When
        userService.registerUser(validRegistrationDTO);
        
        // Then
        verify(passwordEncoder).encode("Password123");
    }
    
    @Test
    @DisplayName("Authenticate User - Empty Email")
    void testAuthenticateUser_EmptyEmail() {
        // When & Then
        assertThrows(
                Exception.class,
                () -> userService.authenticateUser("", "Password123")
        );
    }
    
    @Test
    @DisplayName("Authenticate User - Empty Password")
    void testAuthenticateUser_EmptyPassword() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches("", "encodedPassword")).thenReturn(false);
        
        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.authenticateUser("john.doe@example.com", "")
        );
    }
}
