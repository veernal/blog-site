package com.blogsite.command.repository;

import com.blogsite.command.entity.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Blog Repository for Command Service
 */
@Repository
public interface BlogCommandRepository extends MongoRepository<Blog, String> {
    
    /**
     * Find blog by name and user
     */
    Optional<Blog> findByBlogNameAndUserIdAndDeletedFalse(String blogName, String userId);
    
    /**
     * Check if blog exists by name for a user
     */
    boolean existsByBlogNameAndUserIdAndDeletedFalse(String blogName, String userId);
    
    /**
     * Find active blog by name
     */
    @Query("{ 'blogName': ?0, 'userId': ?1, 'deleted': false }")
    Optional<Blog> findActiveBlog(String blogName, String userId);
}
