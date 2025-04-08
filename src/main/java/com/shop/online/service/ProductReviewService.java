package com.shop.online.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.online.dto.review.ReviewDTO;
import com.shop.online.entity.ProductReview;
import com.shop.online.vo.review.ProductReviewSummaryVO;
import com.shop.online.vo.review.ReviewVO;

/**
 * Service interface for product reviews
 */
public interface ProductReviewService extends IService<ProductReview> {

    /**
     * Submit a product review
     *
     * @param reviewDTO Review data
     * @return Review ID
     */
    Long submitReview(ReviewDTO reviewDTO);
    
    /**
     * Get product review summary (average rating, etc.)
     *
     * @param productId Product ID
     * @return Review summary
     */
    ProductReviewSummaryVO getReviewSummary(Long productId);
    
    /**
     * Get product reviews with pagination
     *
     * @param productId Product ID
     * @param page Page number
     * @param size Page size
     * @return Page of reviews
     */
    Page<ReviewVO> getProductReviews(Long productId, Integer page, Integer size);
    
    /**
     * Check if user has already reviewed a product in an order
     *
     * @param userId User ID
     * @param orderNo Order number
     * @param productId Product ID
     * @return true if already reviewed
     */
    boolean hasReviewed(Long userId, String orderNo, Long productId);
} 