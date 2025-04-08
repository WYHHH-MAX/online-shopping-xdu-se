package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.dto.review.ReviewDTO;
import com.shop.online.entity.ProductReview;
import com.shop.online.entity.User;
import com.shop.online.exception.BusinessException;
import com.shop.online.mapper.ProductReviewMapper;
import com.shop.online.service.OrderService;
import com.shop.online.service.ProductReviewService;
import com.shop.online.service.UserService;
import com.shop.online.vo.OrderVO;
import com.shop.online.vo.review.ProductReviewSummaryVO;
import com.shop.online.vo.review.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service implementation for product reviews
 */
@Service
@Slf4j
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReview(ReviewDTO reviewDTO) {
        // Get current user
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("Please login first");
        }
        
        // Verify that the order exists and belongs to the user
        OrderVO order = orderService.getOrderDetail(reviewDTO.getOrderNo());
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        
        // Verify order status is "completed"
        if (!"3".equals(order.getStatus())) {
            throw new BusinessException("Only completed orders can be reviewed");
        }
        
        // Check if the product is in the order
        boolean productFound = order.getProducts().stream()
                .anyMatch(p -> p.getId() != null && reviewDTO.getProductId() != null && 
                        p.getId().longValue() == reviewDTO.getProductId().longValue());
        if (!productFound) {
            throw new BusinessException("Product not in this order");
        }
        
        // Check if already reviewed
        boolean alreadyReviewed = hasReviewed(currentUser.getId(), reviewDTO.getOrderNo(), reviewDTO.getProductId());
        if (alreadyReviewed) {
            throw new BusinessException("You have already reviewed this product");
        }
        
        // Create review entity
        ProductReview review = new ProductReview();
        review.setOrderNo(reviewDTO.getOrderNo());
        review.setUserId(currentUser.getId());
        review.setProductId(reviewDTO.getProductId());
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        
        // Handle images
        if (reviewDTO.getImages() != null && !reviewDTO.getImages().isEmpty()) {
            String imagesStr = String.join(",", reviewDTO.getImages());
            review.setImages(imagesStr);
        }
        
        review.setCreatedTime(LocalDateTime.now());
        review.setUpdatedTime(LocalDateTime.now());
        review.setDeleted(0);
        
        // Save the review
        boolean saved = save(review);
        if (!saved) {
            throw new BusinessException("Failed to submit review");
        }
        
        return review.getId();
    }
    
    @Override
    public ProductReviewSummaryVO getReviewSummary(Long productId) {
        ProductReviewSummaryVO summary = new ProductReviewSummaryVO();
        summary.setProductId(productId);
        
        // Get average rating
        Double avgRating = baseMapper.getAverageRating(productId);
        summary.setAverageRating(avgRating);
        
        // Get total reviews
        Integer reviewCount = baseMapper.countReviews(productId);
        summary.setReviewCount(reviewCount);
        
        // Get rating distribution
        ProductReviewSummaryVO.RatingDistribution distribution = new ProductReviewSummaryVO.RatingDistribution();
        distribution.setFiveStarCount(baseMapper.countReviewsByRating(productId, 5));
        distribution.setFourStarCount(baseMapper.countReviewsByRating(productId, 4));
        distribution.setThreeStarCount(baseMapper.countReviewsByRating(productId, 3));
        distribution.setTwoStarCount(baseMapper.countReviewsByRating(productId, 2));
        distribution.setOneStarCount(baseMapper.countReviewsByRating(productId, 1));
        summary.setRatingDistribution(distribution);
        
        return summary;
    }
    
    @Override
    public Page<ReviewVO> getProductReviews(Long productId, Integer page, Integer size) {
        // Create MyBatis Plus Page
        Page<ProductReview> pageParam = new Page<>(page, size);
        
        // Query condition
        LambdaQueryWrapper<ProductReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductReview::getProductId, productId);
        queryWrapper.orderByDesc(ProductReview::getCreatedTime);
        
        // Execute query
        Page<ProductReview> reviewPage = page(pageParam, queryWrapper);
        
        // Convert to VO
        Page<ReviewVO> resultPage = new Page<>();
        resultPage.setCurrent(reviewPage.getCurrent());
        resultPage.setSize(reviewPage.getSize());
        resultPage.setTotal(reviewPage.getTotal());
        
        if (reviewPage.getRecords().isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        
        // Get all user IDs in the reviews
        List<Long> userIds = reviewPage.getRecords().stream()
                .map(ProductReview::getUserId)
                .collect(Collectors.toList());
        
        // Get user information in batch
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(User::getId, userIds);
        List<User> users = userService.list(userQuery);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // Transform entities to VOs
        List<ReviewVO> voList = reviewPage.getRecords().stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(review, vo);
            
            // Parse image URLs
            if (StringUtils.hasText(review.getImages())) {
                List<String> images = Arrays.asList(review.getImages().split(","));
                vo.setImages(images);
            }
            
            // Set user information
            User user = userMap.get(review.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(voList);
        return resultPage;
    }
    
    @Override
    public boolean hasReviewed(Long userId, String orderNo, Long productId) {
        Integer count = baseMapper.hasReviewed(userId, orderNo, productId);
        return count > 0;
    }
} 