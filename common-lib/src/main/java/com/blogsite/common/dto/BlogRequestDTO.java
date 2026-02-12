package com.blogsite.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a new blog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequestDTO {
    
    @NotBlank(message = "Blog name is mandatory")
    @Size(min = 20, message = "Blog name should be minimum 20 characters")
    private String blogName;
    
    @NotBlank(message = "Category is mandatory")
    @Size(min = 20, message = "Category should be minimum 20 characters")
    private String category;
    
    @NotBlank(message = "Article is mandatory")
    @Size(min = 1000, message = "Article should be minimum 1000 words")
    private String article;
    
    @NotBlank(message = "Author name is mandatory")
    private String authorName;
}
