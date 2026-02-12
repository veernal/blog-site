package com.blogsite.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Blog Command Service Application
 * Port: 8082
 * Handles all write operations (Commands) for blogs - CQRS Pattern
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class BlogCommandServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BlogCommandServiceApplication.class, args);
    }
}
