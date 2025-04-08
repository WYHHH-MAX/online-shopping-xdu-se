package com.shop.online.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.online.common.result.Result;
import com.shop.online.dto.review.ReviewDTO;
import com.shop.online.entity.User;
import com.shop.online.service.ProductReviewService;
import com.shop.online.service.UserService;
import com.shop.online.vo.review.ProductReviewSummaryVO;
import com.shop.online.vo.review.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for product reviews
 */
@RestController
@RequestMapping("/review")
@Slf4j
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Submit a product review
     */
    @PostMapping("/submit")
    public Result<Long> submitReview(@RequestBody @Validated ReviewDTO reviewDTO) {
        try {
            Long reviewId = productReviewService.submitReview(reviewDTO);
            return Result.success(reviewId);
        } catch (Exception e) {
            log.error("Failed to submit review", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * Get product review summary
     */
    @GetMapping("/summary/{productId}")
    public Result<ProductReviewSummaryVO> getReviewSummary(@PathVariable Long productId) {
        try {
            ProductReviewSummaryVO summary = productReviewService.getReviewSummary(productId);
            return Result.success(summary);
        } catch (Exception e) {
            log.error("Failed to get review summary", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * Get product reviews with pagination
     */
    @GetMapping("/list/{productId}")
    public Result<Page<ReviewVO>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<ReviewVO> reviews = productReviewService.getProductReviews(productId, page, size);
            return Result.success(reviews);
        } catch (Exception e) {
            log.error("Failed to get product reviews", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * Check if current user has reviewed a product from a specific order
     */
    @GetMapping("/check")
    public Result<Boolean> hasReviewed(
            @RequestParam String orderNo,
            @RequestParam Long productId) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.success(false);
            }
            
            boolean hasReviewed = productReviewService.hasReviewed(
                    currentUser.getId(), orderNo, productId);
            return Result.success(hasReviewed);
        } catch (Exception e) {
            log.error("Failed to check review status", e);
            return Result.error(e.getMessage());
        }
    }
} 