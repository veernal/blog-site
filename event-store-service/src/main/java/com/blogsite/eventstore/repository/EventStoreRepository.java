package com.blogsite.eventstore.repository;

import com.blogsite.eventstore.entity.EventStore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Event Store Repository
 */
@Repository
public interface EventStoreRepository extends MongoRepository<EventStore, String> {
    
    /**
     * Find all events for an aggregate
     */
    List<EventStore> findByAggregateIdOrderByVersionAsc(String aggregateId);
    
    /**
     * Find events by user
     */
    List<EventStore> findByUserIdOrderByTimestampDesc(String userId);
    
    /**
     * Find events by type
     */
    List<EventStore> findByEventTypeOrderByTimestampDesc(String eventType);
    
    /**
     * Get latest version for aggregate
     */
    @Query(value = "{ 'aggregateId': ?0 }", sort = "{ 'version': -1 }")
    EventStore findLatestEventByAggregateId(String aggregateId);
}
