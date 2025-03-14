import request from '@/utils/request'
import type { ProductVO, SearchProductsParams } from '@/types/product'

/**
 * 搜索商品
 */
export const searchProducts = (params: SearchProductsParams) => {
  return request<{
    records: ProductVO[]
    total: number
    size: number
    current: number
    pages: number
  }>({
    url: '/product/search',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 */
export const getProductDetail = (id: number) => {
  return request<ProductVO>({
    url: `/product/${id}`,
    method: 'get'
  })
}

/**
 * 获取推荐商品
 */
export const getFeaturedProducts = () => {
  return request<ProductVO[]>({
    url: '/product/featured',
    method: 'get',
    params: {
      page: 1,
      size: 5
    }
  })
}