package com.blogsite.command.controller;

import com.blogsite.common.dto.ApiResponse;
import com.blogsite.common.dto.BlogRequestDTO;
import com.blogsite.common.dto.BlogResponseDTO;
import com.blogsite.command.service.BlogCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Blog Command Controller
 * Handles write operations (Commands) for blogs
 */
@RestController
@RequestMapping("/api/v1.0/blogsite/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Blog Command Operations", description = "APIs for adding and deleting blogs (Write Operations)")
@SecurityRequirement(name = "Bearer Authentication")
public class BlogCommandController {
    
    private final BlogCommandService blogCommandService;
    
    /**
     * US_02: Add new blog
     * POST /api/v1.0/blogsite/user/blogs/add/{blogname}
     */
    @PostMapping("/blogs/add/{blogname}")
    @Operation(summary = "Add a new blog", description = "Add a new blog to the system (Secured)")
    public ResponseEntity<ApiResponse<BlogResponseDTO>> addBlog(
            @PathVariable String blogname,
            @Valid @RequestBody BlogRequestDTO blogRequest,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("POST /api/v1.0/blogsite/user/blogs/add/{} - Add blog by user: {}", blogname, userEmail);
        
        // Validate blog name matches path variable
        if (!blogname.equals(blogRequest.getBlogName())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Blog name in path and body do not match"));
        }
        
        ApiResponse<BlogResponseDTO> response = blogCommandService.addBlog(blogRequest, userEmail, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * US_03: Delete blog
     * DELETE /api/v1.0/blogsite/user/delete/{blogname}
     */
    @DeleteMapping("/delete/{blogname}")
    @Operation(summary = "Delete a blog", description = "Delete a blog from the system (Secured)")
    public ResponseEntity<ApiResponse<Void>> deleteBlog(
            @PathVariable String blogname,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("DELETE /api/v1.0/blogsite/user/delete/{} - Delete blog by user: {}", blogname, userId);
        
        ApiResponse<Void> response = blogCommandService.deleteBlog(blogname, userId);
        return ResponseEntity.ok(response);
    }
}
