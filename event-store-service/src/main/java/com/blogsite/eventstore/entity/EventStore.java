package com.blogsite.eventstore.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Event Store Entity
 * Stores all domain events for audit and replay
 */
@Document(collection = "event_store")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStore {
    
    @Id
    private String eventId;
    
    @Indexed
    private String eventType;
    
    @Indexed
    private String aggregateId;
    
    @Indexed
    private String userId;
    
    private Object payload;
    
    @Indexed
    private LocalDateTime timestamp;
    
    private long version;
}
