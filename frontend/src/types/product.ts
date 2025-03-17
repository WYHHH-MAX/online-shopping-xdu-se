export interface ProductVO {
  id: number
  name: string
  description: string
  price: number
  mainImage: string
  sales: number
  categoryId: number
  categoryName?: string
  sellerId: number
  sellerName?: string
  stock: number
  status: number
  isFeatured: number
  featuredSort: number
  createdTime?: string
  updatedTime?: string
  images: string[]
}

export interface ProductQueryParams {
  keyword?: string
  categoryId?: number
  page?: number
  size?: number
  minPrice?: number
  maxPrice?: number
  orderBy?: string
  isAsc?: boolean
}

export interface SearchProductsParams {
  keyword?: string
  categoryId?: number
  page?: number
  size?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
} 