import request from '@/utils/request'
import type { ProductVO, SearchProductsParams } from '@/types/product'
import type { PageResult } from '../types/common'

/**
 * 搜索商品
 */
export function searchProducts(params: { keyword: string, page?: number, size?: number, sortBy?: string }) {
  return request<any>({
    url: '/public/search',
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
 * 获取推荐商品列表
 * @param page 页码
 * @param size 每页大小
 * @returns 推荐商品列表
 */
export function getFeaturedProducts(page: number = 1, size: number = 8) {
  return request<{
    total: number;
    list: ProductVO[];
  }>({
    url: '/product/featured',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 根据分类获取商品列表
 */
export const getProductsByCategory = (
  categoryId: number,
  page: number,
  size: number,
  sortBy?: string
) => {
  return request<any>({
    url: `/product/category/${categoryId}`,
    method: 'get',
    params: {
      page,
      size,
      sortBy
    }
  }).then(res => {
    // 处理返回数据结构
    if (res && res.data) {
      // 如果数据包裹在data字段中
      return res.data;
    }
    return res;
  })
}

/**
 * 获取新品列表
 * @param page 页码
 * @param size 每页大小
 * @returns 新品列表
 */
export function getNewProducts(page: number = 1, size: number = 8) {
  return request<{
    total: number;
    list: ProductVO[];
  }>({
    url: '/product/new',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取热销商品列表
 * @param page 页码
 * @param size 每页大小
 * @returns 热销商品列表
 */
export function getHotProducts(page: number = 1, size: number = 8) {
  return request<{
    total: number;
    list: ProductVO[];
  }>({
    url: '/product/hot',
    method: 'get',
    params: { page, size }
  })
}
