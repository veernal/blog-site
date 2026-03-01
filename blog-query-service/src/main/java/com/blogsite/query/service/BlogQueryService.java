package com.blogsite.query.service;

import com.blogsite.common.dto.BlogResponseDTO;
import com.blogsite.common.exception.ResourceNotFoundException;
import com.blogsite.query.entity.BlogReadModel;
import com.blogsite.query.repository.BlogQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Blog Query Service
 * Handles all read operations for blogs (CQRS Query)
 * Uses Builder Pattern for composing response DTOs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogQueryService {
    
    private final BlogQueryRepository blogQueryRepository;
    
    /**
     * US_04: Get blogs by category
     * GET /api/v1.0/blogsite/blogs/info/{category}
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogsByCategory", key = "#category")
    public List<BlogResponseDTO> getBlogsByCategory(String category) {
        log.info("Fetching blogs for category: {}", category);
        
        List<BlogReadModel> blogs = blogQueryRepository
                .findByCategoryAndDeletedFalseOrderByCreatedAtDesc(category);
        
        if (blogs.isEmpty()) {
            throw new ResourceNotFoundException("No blogs found for category: " + category);
        }
        
        log.info("Found {} blogs for category: {}", blogs.size(), category);
        
        // Use Builder Pattern to create response DTOs
        return blogs.stream()
                .map(this::toBlogResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * US_03: Get all blogs by user
     * GET /api/v1.0/blogsite/user/getall
     */
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByUser(String userId) {
        log.info("Fetching blogs for user: {}", userId);
        
        List<BlogReadModel> blogs = blogQueryRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
        
        if (blogs.isEmpty()) {
            log.info("No blogs found for user: {}", userId);
            throw new ResourceNotFoundException("No blogs found for user: " + userId);
        }
        
        log.info("Found {} blogs for user: {}", blogs.size(), userId);
        
        return blogs.stream()
                .map(this::toBlogResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * US_04: Get blogs by category and duration
     * GET /api/v1.0/blogsite/blogs/get/{category}/{durationFromRange}/{durationToRange}
     * Uses Builder Pattern (Creational Design Pattern)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogsByCategoryAndDuration", key = "#category + '-' + #fromDate + '-' + #toDate")
    public List<BlogResponseDTO> getBlogsByCategoryAndDuration(String category, String fromDate, String toDate) {
        log.info("Fetching blogs for category: {} between {} and {}", category, fromDate, toDate);
        
        // Parse dates
        LocalDateTime from = parseDate(fromDate).atStartOfDay();
        LocalDateTime to = parseDate(toDate).atTime(23, 59, 59);
        
        // Query blogs
        List<BlogReadModel> blogs = blogQueryRepository
                .findByCategoryAndDateRange(category, from, to);
        
        if (blogs.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("No blogs found for category '%s' between %s and %s", category, fromDate, toDate));
        }
        
        log.info("Found {} blogs for category: {} in date range", blogs.size(), category);
        
        // Build response using Builder Pattern
        return blogs.stream()
                .map(this::toBlogResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert BlogReadModel to BlogResponseDTO using Builder Pattern
     */
    private BlogResponseDTO toBlogResponseDTO(BlogReadModel blog) {
        return BlogResponseDTO.builder()
                .blogId(blog.getBlogId())
                .blogName(blog.getBlogName())
                .category(blog.getCategory())
                .article(blog.getArticle())
                .authorName(blog.getAuthorName())
                .authorEmail(blog.getAuthorEmail())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }
    
    /**
     * Parse date from string (supports multiple formats)
     */
    private LocalDate parseDate(String dateStr) {
        try {
            // Try yyyy-MM-dd format
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            try {
                // Try dd-MM-yyyy format
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd or dd-MM-yyyy");
            }
        }
    }
}
