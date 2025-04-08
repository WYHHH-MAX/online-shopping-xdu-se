package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Product Review Entity
 */
@Data
@TableName("product_review")
public class ProductReview {
    
    /**
     * Review ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * Order Number
     */
    private String orderNo;
    
    /**
     * User ID (reviewer)
     */
    private Long userId;
    
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
     * Review images (comma separated URLs)
     */
    private String images;
    
    /**
     * Creation time
     */
    private LocalDateTime createdTime;
    
    /**
     * Update time
     */
    private LocalDateTime updatedTime;
    
    /**
     * Soft delete flag
     */
    @TableLogic
    private Integer deleted;
} 