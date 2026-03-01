package com.blogsite.query.service;

import com.blogsite.common.event.BlogEvent;
import com.blogsite.query.entity.BlogReadModel;
import com.blogsite.query.repository.BlogQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
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
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Auto-sync blogs on application startup (for local dev without Kafka)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready - triggering automatic blog sync from command service");
        syncBlogsFromCommand();
    }
    
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
    
    /**
     * Sync blogs from command service collection to read model (for local dev without Kafka)
     */
    public void syncBlogsFromCommand() {
        try {
            // Query all non-deleted blogs from the command collection
            Query query = new Query(Criteria.where("deleted").is(false));
            List<Map> commandBlogs = mongoTemplate.find(query, Map.class, "blogs");
            
            log.info("Syncing {} blogs from command collection", commandBlogs.size());
            
            List<String> ids = new ArrayList<>();
            for (Map blog : commandBlogs) {
                try {
                    // command document uses _id, not blogId
                    String id = null;
                    Object rawId = blog.get("_id");
                    if (rawId != null) {
                        id = rawId.toString();
                    } else {
                        id = (String) blog.get("blogId");
                    }
                    ids.add(id);

                    // convert various possible date representations to LocalDateTime
                    LocalDateTime createdAt = convertToLocalDateTime(blog.get("createdAt"));
                    LocalDateTime updatedAt = convertToLocalDateTime(blog.get("updatedAt"));

                    BlogReadModel readModel = BlogReadModel.builder()
                            .blogId(id)
                            .blogName((String) blog.get("blogName"))
                            .category((String) blog.get("category"))
                            .article((String) blog.get("article"))
                            .authorName((String) blog.get("authorName"))
                            .authorEmail((String) blog.get("authorEmail"))
                            .userId((String) blog.get("userId"))
                            .createdAt(createdAt)
                            .updatedAt(updatedAt)
                            .deleted(false)
                            .build();
                    
                    blogQueryRepository.save(readModel);
                    log.info("Synced blog: {}", readModel.getBlogId());
                } catch (Exception e) {
                    log.error("Error syncing blog", e);
                }
            }
            
            // remove any read-model entries that no longer exist in command (including deleted).
            if (ids.isEmpty()) {
                // no active blogs — clear read model completely
                blogQueryRepository.deleteAll();
                log.info("No active command blogs found; cleared read-model collection");
            } else {
                blogQueryRepository.deleteByBlogIdNotIn(ids);
                log.info("Removed stale read-model entries not present in command store");
            }
            
            log.info("Blog sync completed successfully");
            
        } catch (Exception e) {
            log.error("Error syncing blogs from command collection", e);
        }
    }

    /**
     * Helper to normalize various date formats stored in Mongo into LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return Instant.ofEpochMilli(((Number) obj).longValue())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            if (obj instanceof java.util.Date) {
                return Instant.ofEpochMilli(((java.util.Date) obj).getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            // handle BSON types without adding a hard dependency on driver
            String className = obj.getClass().getSimpleName();
            if (className.equals("BsonDateTime")) {
                long v = (long) obj.getClass().getMethod("getValue").invoke(obj);
                return Instant.ofEpochMilli(v).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            if (className.equals("BsonInt64")) {
                long v = (long) obj.getClass().getMethod("getValue").invoke(obj);
                return Instant.ofEpochMilli(v).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            // fall back to Jackson, which may still throw
            return objectMapper.convertValue(obj, LocalDateTime.class);
        } catch (Exception e) {
            log.warn("Unable to convert object to LocalDateTime: {} (type {})", obj, obj.getClass(), e);
            return null;
        }
    }
}
