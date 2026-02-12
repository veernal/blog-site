package com.blogsite.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Blog information
 * Uses Builder pattern for composing model objects (Creational Design Pattern)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponseDTO {
    
    private String blogId;
    
    private String blogName;
    
    private String category;
    
    private String article;
    
    private String authorName;
    
    private String authorEmail;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create a builder with default timestamp
     */
    public static BlogResponseDTOBuilder builderWithTimestamp() {
        return builder().createdAt(LocalDateTime.now());
    }
    
    /**
     * Creates a response with author details embedded
     */
    public static BlogResponseDTO withAuthorDetails(String blogId, String blogName, 
                                                     String category, String article,
                                                     String authorName, String authorEmail,
                                                     LocalDateTime createdAt) {
        return BlogResponseDTO.builder()
                .blogId(blogId)
                .blogName(blogName)
                .category(category)
                .article(article)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .createdAt(createdAt)
                .build();
    }
}
