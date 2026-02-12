package com.blogsite.eventstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Event Store Service Application
 * Port: 8084
 * Stores all events for CQRS Event Sourcing
 */
@SpringBootApplication
@EnableDiscoveryClient
public class EventStoreServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EventStoreServiceApplication.class, args);
    }
}
