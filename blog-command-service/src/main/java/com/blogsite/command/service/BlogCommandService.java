package com.blogsite.command.service;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.BlogRequestDTO;
import com.blogsite.common.dto.BlogResponseDTO;
import com.blogsite.common.event.BlogEvent;
import com.blogsite.common.exception.DuplicateResourceException;
import com.blogsite.common.exception.ResourceNotFoundException;
import com.blogsite.command.entity.Blog;
import com.blogsite.command.repository.BlogCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Blog Command Service
 * Handles all write operations for blogs (CQRS Command)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogCommandService {
    
    private final BlogCommandRepository blogCommandRepository;
    private final EventPublisherService eventPublisherService;
    
    /**
     * US_02: Add new blog
     * POST /api/v1.0/blogsite/user/blogs/add/{blogname}
     */
    @Transactional
    public ApiResponse<BlogResponseDTO> addBlog(BlogRequestDTO blogRequest, String userEmail, String userId) {
        log.info("Adding new blog: {} by user: {}", blogRequest.getBlogName(), userEmail);
        
        // Check if blog already exists for the user
        if (blogCommandRepository.existsByBlogNameAndUserIdAndDeletedFalse(blogRequest.getBlogName(), userId)) {
            throw new DuplicateResourceException("Blog with name '" + blogRequest.getBlogName() + "' already exists for this user");
        }
        
        // Create blog entity
        Blog blog = Blog.builder()
                .blogId(UUID.randomUUID().toString())
                .blogName(blogRequest.getBlogName())
                .category(blogRequest.getCategory())
                .article(blogRequest.getArticle())
                .authorName(blogRequest.getAuthorName())
                .authorEmail(userEmail)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
        
        // Save blog
        Blog savedBlog = blogCommandRepository.save(blog);
        log.info("Blog saved successfully with ID: {}", savedBlog.getBlogId());
        
        // Publish event for CQRS
        publishBlogCreatedEvent(savedBlog);
        
        // Build response using Builder pattern
        BlogResponseDTO response = BlogResponseDTO.builder()
                .blogId(savedBlog.getBlogId())
                .blogName(savedBlog.getBlogName())
                .category(savedBlog.getCategory())
                .article(savedBlog.getArticle())
                .authorName(savedBlog.getAuthorName())
                .authorEmail(savedBlog.getAuthorEmail())
                .createdAt(savedBlog.getCreatedAt())
                .updatedAt(savedBlog.getUpdatedAt())
                .build();
        
        return ApiResponse.success(response, "Blog added successfully");
    }
    
    /**
     * US_03: Delete blog
     * DELETE /api/v1.0/blogsite/user/delete/{blogname}
     */
    @Transactional
    public ApiResponse<Void> deleteBlog(String blogName, String userId) {
        log.info("Deleting blog: {} for user: {}", blogName, userId);
        long start = System.currentTimeMillis();
        
        // Find blog
        Blog blog = blogCommandRepository.findByBlogNameAndUserIdAndDeletedFalse(blogName, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog '" + blogName + "' not found"));
        log.info("Lookup took {} ms", System.currentTimeMillis() - start);
        
        // Soft delete
        start = System.currentTimeMillis();
        blog.setDeleted(true);
        blog.setUpdatedAt(LocalDateTime.now());
        blogCommandRepository.save(blog);
        log.info("Save took {} ms", System.currentTimeMillis() - start);
        
        log.info("Blog deleted successfully: {}", blogName);
        
        // Publish event for CQRS
        publishBlogDeletedEvent(blog);
        
        return ApiResponse.success(null, "Blog deleted successfully");
    }
    
    /**
     * Publish Blog Created Event
     */
    private void publishBlogCreatedEvent(Blog blog) {
        BlogEvent event = BlogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BlogEvent.EventType.BLOG_CREATED.name())
                .aggregateId(blog.getBlogId())
                .userId(blog.getUserId())
                .payload(blog)
                .timestamp(LocalDateTime.now())
                .build();
        
        eventPublisherService.publishEvent(event);
    }
    
    /**
     * Publish Blog Deleted Event
     */
    private void publishBlogDeletedEvent(Blog blog) {
        BlogEvent event = BlogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BlogEvent.EventType.BLOG_DELETED.name())
                .aggregateId(blog.getBlogId())
                .userId(blog.getUserId())
                .payload(blog)
                .timestamp(LocalDateTime.now())
                .build();
        
        eventPublisherService.publishEvent(event);
    }
}
