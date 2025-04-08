package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.online.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * Mapper interface for product reviews
 */
@Mapper
@Repository
public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    /**
     * Calculate average rating for a product
     *
     * @param productId Product ID
     * @return Average rating or 0 if no reviews
     */
    @Select("SELECT COALESCE(AVG(rating), 0) FROM product_review WHERE product_id = #{productId} AND deleted = 0")
    Double getAverageRating(@Param("productId") Long productId);
    
    /**
     * Count reviews for a product
     *
     * @param productId Product ID
     * @return Number of reviews
     */
    @Select("SELECT COUNT(*) FROM product_review WHERE product_id = #{productId} AND deleted = 0")
    Integer countReviews(@Param("productId") Long productId);
    
    /**
     * Count reviews with specific rating
     *
     * @param productId Product ID
     * @param rating Rating (1-5)
     * @return Number of reviews with the specified rating
     */
    @Select("SELECT COUNT(*) FROM product_review WHERE product_id = #{productId} AND rating = #{rating} AND deleted = 0")
    Integer countReviewsByRating(@Param("productId") Long productId, @Param("rating") Integer rating);
    
    /**
     * Check if user has already reviewed a product in an order
     *
     * @param userId User ID
     * @param orderNo Order number
     * @param productId Product ID
     * @return Number of existing reviews (should be 0 or 1)
     */
    @Select("SELECT COUNT(*) FROM product_review WHERE user_id = #{userId} AND order_no = #{orderNo} AND product_id = #{productId} AND deleted = 0")
    Integer hasReviewed(@Param("userId") Long userId, @Param("orderNo") String orderNo, @Param("productId") Long productId);
} 