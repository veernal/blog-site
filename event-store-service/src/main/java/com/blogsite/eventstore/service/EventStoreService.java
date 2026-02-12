package com.blogsite.eventstore.service;

import com.blogsite.common.event.BlogEvent;
import com.blogsite.eventstore.entity.EventStore;
import com.blogsite.eventstore.repository.EventStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Event Store Service
 * Manages event storage and retrieval
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreService {
    
    private final EventStoreRepository eventStoreRepository;
    
    /**
     * Store an event
     */
    @Transactional
    public EventStore storeEvent(BlogEvent blogEvent) {
        log.info("Storing event: {} for aggregate: {}", blogEvent.getEventType(), blogEvent.getAggregateId());
        
        // Get next version
        long version = getNextVersion(blogEvent.getAggregateId());
        
        // Create event store entity
        EventStore eventStore = EventStore.builder()
                .eventId(blogEvent.getEventId())
                .eventType(blogEvent.getEventType())
                .aggregateId(blogEvent.getAggregateId())
                .userId(blogEvent.getUserId())
                .payload(blogEvent.getPayload())
                .timestamp(blogEvent.getTimestamp())
                .version(version)
                .build();
        
        EventStore savedEvent = eventStoreRepository.save(eventStore);
        log.info("Event stored successfully: {} version: {}", savedEvent.getEventId(), version);
        
        return savedEvent;
    }
    
    /**
     * Get all events for an aggregate (for replay)
     */
    @Transactional(readOnly = true)
    public List<EventStore> getEventsByAggregate(String aggregateId) {
        return eventStoreRepository.findByAggregateIdOrderByVersionAsc(aggregateId);
    }
    
    /**
     * Get events by user
     */
    @Transactional(readOnly = true)
    public List<EventStore> getEventsByUser(String userId) {
        return eventStoreRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    /**
     * Get next version for aggregate
     */
    private long getNextVersion(String aggregateId) {
        EventStore latestEvent = eventStoreRepository.findLatestEventByAggregateId(aggregateId);
        return latestEvent != null ? latestEvent.getVersion() + 1 : 1;
    }
}
