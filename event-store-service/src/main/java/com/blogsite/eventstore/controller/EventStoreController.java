package com.blogsite.eventstore.controller;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.event.BlogEvent;
import com.blogsite.eventstore.entity.EventStore;
import com.blogsite.eventstore.service.EventStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Event Store Controller
 * Manages event storage and retrieval
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventStoreController {
    
    private final EventStoreService eventStoreService;
    
    /**
     * Store an event
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EventStore>> storeEvent(@RequestBody BlogEvent blogEvent) {
        log.info("POST /api/events - Store event: {}", blogEvent.getEventId());
        EventStore storedEvent = eventStoreService.storeEvent(blogEvent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(storedEvent, "Event stored successfully"));
    }
    
    /**
     * Get events by aggregate ID
     */
    @GetMapping("/aggregate/{aggregateId}")
    public ResponseEntity<ApiResponse<List<EventStore>>> getEventsByAggregate(@PathVariable String aggregateId) {
        log.info("GET /api/events/aggregate/{} - Get events by aggregate", aggregateId);
        List<EventStore> events = eventStoreService.getEventsByAggregate(aggregateId);
        return ResponseEntity.ok(ApiResponse.success(events, "Events fetched successfully"));
    }
    
    /**
     * Get events by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<EventStore>>> getEventsByUser(@PathVariable String userId) {
        log.info("GET /api/events/user/{} - Get events by user", userId);
        List<EventStore> events = eventStoreService.getEventsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(events, "Events fetched successfully"));
    }
}
