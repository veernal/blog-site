package com.blogsite.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Blog Query Service Application
 * Port: 8083
 * Handles all read operations (Queries) for blogs - CQRS Pattern
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableCaching
public class BlogQueryServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BlogQueryServiceApplication.class, args);
    }
}
