package com.blogsite.query.service;

import com.blogsite.common.event.BlogEvent;
import com.blogsite.query.entity.BlogReadModel;
import com.blogsite.query.repository.BlogQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Event Consumer Service
 * Listens to blog events and updates read model
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumerService {
    
    private final BlogQueryRepository blogQueryRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Listen to blog events from Kafka
     */
    @KafkaListener(topics = "blog-events", groupId = "blog-query-service")
    public void consumeBlogEvent(String eventJson) {
        try {
            log.info("Received blog event: {}", eventJson);
            
            BlogEvent event = objectMapper.readValue(eventJson, BlogEvent.class);
            
            switch (BlogEvent.EventType.valueOf(event.getEventType())) {
                case BLOG_CREATED:
                    handleBlogCreated(event);
                    break;
                case BLOG_UPDATED:
                    handleBlogUpdated(event);
                    break;
                case BLOG_DELETED:
                    handleBlogDeleted(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing blog event", e);
        }
    }
    
    /**
     * Handle Blog Created Event
     */
    private void handleBlogCreated(BlogEvent event) {
        try {
            Map<String, Object> payload = objectMapper.convertValue(event.getPayload(), Map.class);
            
            BlogReadModel blog = BlogReadModel.builder()
                    .blogId((String) payload.get("blogId"))
                    .blogName((String) payload.get("blogName"))
                    .category((String) payload.get("category"))
                    .article((String) payload.get("article"))
                    .authorName((String) payload.get("authorName"))
                    .authorEmail((String) payload.get("authorEmail"))
                    .userId((String) payload.get("userId"))
                    .createdAt(objectMapper.convertValue(payload.get("createdAt"), java.time.LocalDateTime.class))
                    .updatedAt(objectMapper.convertValue(payload.get("updatedAt"), java.time.LocalDateTime.class))
                    .deleted(false)
                    .build();
            
            blogQueryRepository.save(blog);
            log.info("Blog read model created: {}", blog.getBlogId());
            
        } catch (Exception e) {
            log.error("Error handling blog created event", e);
        }
    }
    
    /**
     * Handle Blog Updated Event
     */
    private void handleBlogUpdated(BlogEvent event) {
        try {
            Map<String, Object> payload = objectMapper.convertValue(event.getPayload(), Map.class);
            
            blogQueryRepository.findById(event.getAggregateId()).ifPresent(blog -> {
                blog.setBlogName((String) payload.get("blogName"));
                blog.setCategory((String) payload.get("category"));
                blog.setArticle((String) payload.get("article"));
                blog.setUpdatedAt(objectMapper.convertValue(payload.get("updatedAt"), java.time.LocalDateTime.class));
                blogQueryRepository.save(blog);
                log.info("Blog read model updated: {}", blog.getBlogId());
            });
            
        } catch (Exception e) {
            log.error("Error handling blog updated event", e);
        }
    }
    
    /**
     * Handle Blog Deleted Event
     */
    private void handleBlogDeleted(BlogEvent event) {
        try {
            blogQueryRepository.findById(event.getAggregateId()).ifPresent(blog -> {
                blog.setDeleted(true);
                blogQueryRepository.save(blog);
                log.info("Blog read model deleted: {}", blog.getBlogId());
            });
            
        } catch (Exception e) {
            log.error("Error handling blog deleted event", e);
        }
    }
}
