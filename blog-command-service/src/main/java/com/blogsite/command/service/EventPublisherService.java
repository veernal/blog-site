package com.blogsite.command.service;

import com.blogsite.common.event.BlogEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Event Publisher Service
 * Publishes events for CQRS event sourcing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    private static final String BLOG_EVENTS_TOPIC = "blog-events";
    
    /**
     * Publish event to Kafka and Event Store
     */
    public void publishEvent(BlogEvent event) {
        try {
            // Convert event to JSON
            String eventJson = objectMapper.writeValueAsString(event);
            
            // Publish to Kafka for Query Service consumption
            kafkaTemplate.send(BLOG_EVENTS_TOPIC, event.getAggregateId(), eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Event published to Kafka: {} for aggregate: {}", 
                                    event.getEventType(), event.getAggregateId());
                        } else {
                            log.error("Failed to publish event to Kafka", ex);
                        }
                    });
            
            // Store event in Event Store Service (async)
            storeEventAsync(event);
            
        } catch (Exception e) {
            log.error("Error publishing event", e);
        }
    }
    
    /**
     * Store event in Event Store Service asynchronously
     */
    private void storeEventAsync(BlogEvent event) {
        webClientBuilder.build()
                .post()
                .uri("http://event-store-service/api/events")
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        result -> log.info("Event stored in Event Store: {}", event.getEventId()),
                        error -> log.error("Failed to store event in Event Store", error)
                );
    }
}
