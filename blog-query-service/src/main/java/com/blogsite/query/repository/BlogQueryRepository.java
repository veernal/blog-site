package com.blogsite.query.repository;

import com.blogsite.query.entity.BlogReadModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Blog Query Repository
 * Optimized for read operations
 */
@Repository
public interface BlogQueryRepository extends MongoRepository<BlogReadModel, String> {
    
    /**
     * Find blogs by category (active only)
     */
    List<BlogReadModel> findByCategoryAndDeletedFalseOrderByCreatedAtDesc(String category);
    
    /**
     * Find blogs by user
     */
    List<BlogReadModel> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(String userId);
    
    /**
     * Remove any read-model entries whose IDs are not in the provided list
     */
    void deleteByBlogIdNotIn(List<String> ids);
    
    /**
     * Find blogs by category within date range
     */
    @Query("{ 'category': ?0, 'createdAt': { $gte: ?1, $lte: ?2 }, 'deleted': false }")
    List<BlogReadModel> findByCategoryAndDateRange(String category, LocalDateTime from, LocalDateTime to);
    
    /**
     * Search blogs by category with pagination
     */
    List<BlogReadModel> findByCategoryContainingIgnoreCaseAndDeletedFalse(String category);
}
