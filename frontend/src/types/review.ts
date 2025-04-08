export interface ReviewDTO {
  orderNo: string;
  productId: number;
  rating: number;
  content?: string;
  images?: string[];
}

export interface ReviewVO {
  id: number;
  userId: number;
  username: string;
  avatar?: string;
  productId: number;
  rating: number;
  content?: string;
  images?: string[];
  createdTime: string;
}

export interface RatingDistribution {
  fiveStarCount: number;
  fourStarCount: number;
  threeStarCount: number;
  twoStarCount: number;
  oneStarCount: number;
}

export interface ProductReviewSummaryVO {
  productId: number;
  averageRating: number;
  reviewCount: number;
  ratingDistribution: RatingDistribution;
} 