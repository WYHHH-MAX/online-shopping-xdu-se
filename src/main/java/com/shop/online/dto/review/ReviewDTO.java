package com.shop.online.dto.review;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for product review submission
 */
@Data
public class ReviewDTO {
    
    /**
     * Order number (required)
     */
    @NotBlank(message = "Order number cannot be empty")
    private String orderNo;
    
    /**
     * Product ID (required)
     */
    @NotNull(message = "Product ID cannot be empty")
    private Long productId;
    
    /**
     * Rating (1-5 stars, required)
     */
    @NotNull(message = "Rating cannot be empty")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    /**
     * Review content
     */
    private String content;
    
    /**
     * Review images (optional)
     */
    private List<String> images;
} 