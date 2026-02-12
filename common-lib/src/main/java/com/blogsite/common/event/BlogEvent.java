package com.blogsite.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base Event class for CQRS Event Sourcing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogEvent {
    
    private String eventId;
    private String eventType;
    private String aggregateId; // Blog ID
    private String userId;
    private Object payload;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public enum EventType {
        BLOG_CREATED,
        BLOG_UPDATED,
        BLOG_DELETED
    }
}
