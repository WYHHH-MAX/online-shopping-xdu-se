export interface ProductVO {
  id: number
  sellerId: number
  categoryId: number
  name: string
  description: string
  price: number
  stock: number
  status: number
  sales: number
  mainImage: string
  images?: string[]
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