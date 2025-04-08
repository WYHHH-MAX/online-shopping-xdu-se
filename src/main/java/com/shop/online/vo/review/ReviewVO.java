package com.shop.online.vo.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Value object for product review response
 */
@Data
public class ReviewVO {
    
    /**
     * Review ID
     */
    private Long id;
    
    /**
     * User ID
     */
    private Long userId;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * User avatar
     */
    private String avatar;
    
    /**
     * Product ID
     */
    private Long productId;
    
    /**
     * Rating (1-5 stars)
     */
    private Integer rating;
    
    /**
     * Review content
     */
    private String content;
    
    /**
     * Review images
     */
    private List<String> images;
    
    /**
     * Creation time
     */
    private LocalDateTime createdTime;
} 