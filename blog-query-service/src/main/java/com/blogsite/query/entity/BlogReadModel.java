package com.blogsite.query.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Blog Read Model - Optimized for queries
 * Stored in MongoDB (read replica)
 */
@Document(collection = "blog_read_model")
@CompoundIndex(def = "{'category': 1, 'createdAt': -1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogReadModel implements Serializable {
    
    @Id
    private String blogId;
    
    @Indexed
    private String blogName;
    
    @Indexed
    private String category;
    
    private String article;
    
    private String authorName;
    
    private String authorEmail;
    
    @Indexed
    private String userId;
    
    @Indexed
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Indexed
    private boolean deleted = false;
}
