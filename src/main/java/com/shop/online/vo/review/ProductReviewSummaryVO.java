package com.shop.online.vo.review;

import lombok.Data;

/**
 * Value object for product review summary (average rating)
 */
@Data
public class ProductReviewSummaryVO {
    
    /**
     * Product ID
     */
    private Long productId;
    
    /**
     * Average rating
     */
    private Double averageRating;
    
    /**
     * Total number of reviews
     */
    private Integer reviewCount;
    
    /**
     * Distribution of ratings
     */
    private RatingDistribution ratingDistribution;
    
    @Data
    public static class RatingDistribution {
        private Integer fiveStarCount;
        private Integer fourStarCount;
        private Integer threeStarCount;
        private Integer twoStarCount;
        private Integer oneStarCount;
    }
} 