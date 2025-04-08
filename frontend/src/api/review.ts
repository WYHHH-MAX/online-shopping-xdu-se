import request from '@/utils/request';
import type { ProductReviewSummaryVO, ReviewVO } from '@/types/review';
import type { ApiResponse } from '@/types/common';

/**
 * Submit a product review
 * @param reviewData Review data
 * @returns Review ID
 */
export function submitReview(reviewData: any) {
  return request<ApiResponse<number>>({
    url: '/review/submit',
    method: 'post',
    data: reviewData
  });
}

/**
 * Get product review summary
 * @param productId Product ID
 * @returns Product review summary
 */
export function getReviewSummary(productId: number) {
  return request<ApiResponse<ProductReviewSummaryVO>>({
    url: `/review/summary/${productId}`,
    method: 'get'
  });
}

/**
 * Get product reviews with pagination
 * @param productId Product ID
 * @param page Page number
 * @param size Page size
 * @returns Paginated product reviews
 */
export function getProductReviews(productId: number, page: number = 1, size: number = 10) {
  return request<ApiResponse<any>>({
    url: `/review/list/${productId}`,
    method: 'get',
    params: { page, size }
  });
}

/**
 * Check if user has reviewed a product from a specific order
 * @param orderNo Order number
 * @param productId Product ID
 * @returns Has reviewed
 */
export function hasReviewed(orderNo: string, productId: number) {
  return request<ApiResponse<boolean>>({
    url: '/review/check',
    method: 'get',
    params: { orderNo, productId }
  });
} 