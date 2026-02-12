package com.blogsite.command.controller;

import com.blogsite.command.service.BlogCommandService;
import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.BlogRequestDTO;
import com.blogsite.common.dto.BlogResponseDTO;
import com.blogsite.common.exception.DuplicateResourceException;
import com.blogsite.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for BlogCommandController
 * Tests REST API endpoints with positive and negative scenarios
 */
@WebMvcTest(BlogCommandController.class)
@DisplayName("Blog Command Controller Tests")
class BlogCommandControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private BlogCommandService blogCommandService;
    
    private BlogRequestDTO validBlogRequest;
    private BlogResponseDTO blogResponse;
    
    @BeforeEach
    void setUp() {
        // Prepare test data
        validBlogRequest = BlogRequestDTO.builder()
                .blogName("Understanding Microservices Architecture Patterns")
                .category("Software Engineering Best Practices")
                .article(generateLongArticle()) // 1000+ words
                .authorName("John Doe")
                .build();
        
        blogResponse = BlogResponseDTO.builder()
                .blogId("blog-123")
                .blogName("Understanding Microservices Architecture Patterns")
                .category("Software Engineering Best Practices")
                .article(generateLongArticle())
                .authorName("John Doe")
                .authorEmail("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("Add Blog - Success Scenario")
    void testAddBlog_Success() throws Exception {
        // Given
        ApiResponse<BlogResponseDTO> apiResponse = ApiResponse.success(blogResponse, "Blog added successfully");
        when(blogCommandService.addBlog(any(), anyString(), anyString())).thenReturn(apiResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/blogs/add/{blogname}", 
                        "Understanding Microservices Architecture Patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "john.doe@example.com")
                .header("X-User-Id", "user-123")
                .content(objectMapper.writeValueAsString(validBlogRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Blog added successfully"))
                .andExpect(jsonPath("$.data.blogId").value("blog-123"))
                .andExpect(jsonPath("$.data.blogName").value("Understanding Microservices Architecture Patterns"))
                .andExpect(jsonPath("$.data.authorName").value("John Doe"));
    }
    
    @Test
    @DisplayName("Add Blog - Blog Name Mismatch")
    void testAddBlog_BlogNameMismatch() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/blogs/add/{blogname}", 
                        "Different Blog Name")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "john.doe@example.com")
                .header("X-User-Id", "user-123")
                .content(objectMapper.writeValueAsString(validBlogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Blog name in path and body do not match"));
    }
    
    @Test
    @DisplayName("Add Blog - Validation Error (Short Blog Name)")
    void testAddBlog_ValidationError_ShortBlogName() throws Exception {
        // Given
        BlogRequestDTO invalidRequest = BlogRequestDTO.builder()
                .blogName("Short")
                .category("Software Engineering Best Practices")
                .article(generateLongArticle())
                .authorName("John Doe")
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/blogs/add/{blogname}", "Short")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "john.doe@example.com")
                .header("X-User-Id", "user-123")
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Add Blog - Validation Error (Short Article)")
    void testAddBlog_ValidationError_ShortArticle() throws Exception {
        // Given
        BlogRequestDTO invalidRequest = BlogRequestDTO.builder()
                .blogName("Understanding Microservices Architecture Patterns")
                .category("Software Engineering Best Practices")
                .article("Too short article")
                .authorName("John Doe")
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/blogs/add/{blogname}", 
                        "Understanding Microservices Architecture Patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "john.doe@example.com")
                .header("X-User-Id", "user-123")
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Add Blog - Duplicate Blog")
    void testAddBlog_DuplicateBlog() throws Exception {
        // Given
        when(blogCommandService.addBlog(any(), anyString(), anyString()))
                .thenThrow(new DuplicateResourceException("Blog already exists"));
        
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/blogs/add/{blogname}", 
                        "Understanding Microservices Architecture Patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "john.doe@example.com")
                .header("X-User-Id", "user-123")
                .content(objectMapper.writeValueAsString(validBlogRequest)))
                .andExpect(status().isConflict());
    }
    
    @Test
    @DisplayName("Add Blog - Missing Authorization Headers")
    void testAddBlog_MissingHeaders() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1.0/blogsite/user/blogs/add/{blogname}", 
                        "Understanding Microservices Architecture Patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBlogRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Delete Blog - Success Scenario")
    void testDeleteBlog_Success() throws Exception {
        // Given
        ApiResponse<Void> apiResponse = ApiResponse.success(null, "Blog deleted successfully");
        when(blogCommandService.deleteBlog(anyString(), anyString())).thenReturn(apiResponse);
        
        // When & Then
        mockMvc.perform(delete("/api/v1.0/blogsite/user/delete/{blogname}", 
                        "Understanding Microservices Architecture Patterns")
                .header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Blog deleted successfully"));
    }
    
    @Test
    @DisplayName("Delete Blog - Blog Not Found")
    void testDeleteBlog_NotFound() throws Exception {
        // Given
        when(blogCommandService.deleteBlog(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Blog not found"));
        
        // When & Then
        mockMvc.perform(delete("/api/v1.0/blogsite/user/delete/{blogname}", 
                        "Nonexistent Blog")
                .header("X-User-Id", "user-123"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Delete Blog - Missing User ID Header")
    void testDeleteBlog_MissingUserId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1.0/blogsite/user/delete/{blogname}", 
                        "Understanding Microservices Architecture Patterns"))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * Helper method to generate article with 1000+ words
     */
    private String generateLongArticle() {
        StringBuilder article = new StringBuilder();
        String paragraph = "Microservices architecture represents a method of developing software applications " +
                "as a suite of independently deployable, small, modular services. Each service runs a unique " +
                "process and communicates through a well-defined, lightweight mechanism to serve a business goal. ";
        
        // Repeat to reach 1000+ words
        for (int i = 0; i < 50; i++) {
            article.append(paragraph);
        }
        
        return article.toString();
    }
}
