package com.blogsite.query.controller;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.BlogResponseDTO;
import com.blogsite.query.service.BlogQueryService;
import com.blogsite.query.service.EventConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Blog Query Controller
 * Handles read operations (Queries) for blogs
 */
@RestController
@RequestMapping("/api/v1.0/blogsite")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Blog Query Operations", description = "APIs for searching and viewing blogs (Read Operations)")
public class BlogQueryController {
    
    private final BlogQueryService blogQueryService;
    private final EventConsumerService eventConsumerService;
    private final MongoTemplate mongoTemplate;
    
    /**
     * US_04: Get blogs by category
     * GET /api/v1.0/blogsite/blogs/info/{category}
     */
    @GetMapping("/blogs/info/{category}")
    @Operation(summary = "Get blogs by category", description = "Fetch all blogs for a specific category with author details")
    public ResponseEntity<ApiResponse<List<BlogResponseDTO>>> getBlogsByCategory(@PathVariable String category) {
        log.info("GET /api/v1.0/blogsite/blogs/info/{} - Get blogs by category", category);
        
        List<BlogResponseDTO> blogs = blogQueryService.getBlogsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(blogs, "Blogs fetched successfully"));
    }
    
    /**
     * US_03: Get all blogs by user
     * GET /api/v1.0/blogsite/user/getall
     */
    @GetMapping("/user/getall")
    @Operation(summary = "Get all user blogs", description = "Fetch all blogs created by the authenticated user (Secured)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<BlogResponseDTO>>> getBlogsByUser(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("GET /api/v1.0/blogsite/user/getall - Get blogs by user: {}", userId);
        
        List<BlogResponseDTO> blogs = blogQueryService.getBlogsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(blogs, "User blogs fetched successfully"));
    }
    
    /**
     * US_04: Get blogs by category and duration
     * GET /api/v1.0/blogsite/blogs/get/{category}/{durationFromRange}/{durationToRange}
     */
    @GetMapping("/blogs/get/{category}/{durationFromRange}/{durationToRange}")
    @Operation(summary = "Get blogs by category and date range", 
               description = "Fetch blogs by category created within a specific date range. Date format: yyyy-MM-dd")
    public ResponseEntity<ApiResponse<List<BlogResponseDTO>>> getBlogsByCategoryAndDuration(
            @PathVariable String category,
            @PathVariable String durationFromRange,
            @PathVariable String durationToRange) {
        
        log.info("GET /api/v1.0/blogsite/blogs/get/{}/{}/{} - Get blogs by category and duration", 
                 category, durationFromRange, durationToRange);
        
        List<BlogResponseDTO> blogs = blogQueryService.getBlogsByCategoryAndDuration(
                category, durationFromRange, durationToRange);
        
        return ResponseEntity.ok(ApiResponse.success(blogs, "Blogs fetched successfully"));
    }
    
    /**
     * Manual sync endpoint - syncs blogs from command service to read model (for local dev without Kafka)
     * POST /api/v1.0/blogsite/admin/sync-blogs
     */
    @PostMapping("/admin/sync-blogs")
    @Operation(summary = "Sync blogs from command to query (Admin only)", 
               description = "Manually synchronize blogs from command service collection to query read model. Use when Kafka is unavailable.")
    public ResponseEntity<ApiResponse<Void>> syncBlogs() {
        log.info("POST /api/v1.0/blogsite/admin/sync-blogs - Triggering manual sync");
        
        eventConsumerService.syncBlogsFromCommand();
        
        return ResponseEntity.ok(ApiResponse.success(null, "Blog sync initiated successfully"));
    }
    
    /**
     * Debug endpoint for collection counts
     * GET /api/v1.0/blogsite/admin/debug
     */
    @GetMapping("/admin/debug")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugCollections() {
        long commandCount = mongoTemplate.getCollection("blogs").countDocuments();
        long readCount = mongoTemplate.getCollection("blog_read_model").countDocuments();
        Map<String, Object> result = Map.of(
                "commandCollectionCount", commandCount,
                "readModelCollectionCount", readCount
        );
        return ResponseEntity.ok(ApiResponse.success(result, "Debug info"));
    }
    
    /**
     * Fetch raw documents from command and read-model collections
     * GET /api/v1.0/blogsite/admin/raw
     */
    @GetMapping("/admin/raw")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rawCollections() {
        List<Map> commandDocs = mongoTemplate.findAll(Map.class, "blogs");
        List<Map> readDocs = mongoTemplate.findAll(Map.class, "blog_read_model");
        Map<String, Object> result = Map.of(
                "commandDocs", commandDocs,
                "readDocs", readDocs
        );
        return ResponseEntity.ok(ApiResponse.success(result, "Raw documents"));
    }
}
