package com.blogsite.command.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Blog Entity - Domain Model
 * Stored in MongoDB
 */
@Document(collection = "blogs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
    
    @Id
    private String blogId;
    
    @Indexed
    private String blogName;
    
    @Indexed
    private String category;
    
    private String article;
    
    private String authorName;
    
    @Indexed
    private String authorEmail;
    
    private String userId;
    
    @Indexed
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private boolean deleted = false;
}
