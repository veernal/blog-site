package com.blogsite.user.exception;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.exception.DuplicateResourceException;
import com.blogsite.common.exception.ResourceNotFoundException;
import com.blogsite.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler
 */
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    
    @Test
    @DisplayName("Should handle DuplicateResourceException")
    void testHandleDuplicateResource() {
        // Given
        String errorMessage = "User already exists";
        DuplicateResourceException exception = new DuplicateResourceException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleDuplicateResource(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    }
    
    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void testHandleResourceNotFound() {
        // Given
        String errorMessage = "User not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFound(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    }
    
    @Test
    @DisplayName("Should handle ValidationException")
    void testHandleValidation() {
        // Given
        String errorMessage = "Validation failed";
        ValidationException exception = new ValidationException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidation(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    }
    
    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void testHandleValidationErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("userDTO", "email", "Email is invalid");
        FieldError fieldError2 = new FieldError("userDTO", "password", "Password is too short");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        
        // When
        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationErrors(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData().get("email")).isEqualTo("Email is invalid");
        assertThat(response.getBody().getData().get("password")).isEqualTo("Password is too short");
    }
    
    @Test
    @DisplayName("Should handle generic exceptions")
    void testHandleGenericException() {
        // Given
        String errorMessage = "Something went wrong";
        Exception exception = new RuntimeException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred: Something went wrong");
    }
    
    @Test
    @DisplayName("Should handle empty field errors in MethodArgumentNotValidException")
    void testHandleValidationErrors_EmptyErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(List.of());
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        
        // When
        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationErrors(exception);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEmpty();
    }
}
