import request from '@/utils/request'
import type { CategoryVO } from '@/types/category'

/**
 * 获取分类树
 */
export function getCategoryTree() {
  return request<CategoryVO[]>({
    url: '/public/category/tree',
    method: 'get'
  })
}

/**
 * 获取分类列表
 */
export function getCategories(parentId?: number) {
  return request<CategoryVO[]>({
    url: '/public/category/list',
    method: 'get',
    params: { parentId }
  })
}

export function getCategoriesByLevel(level: number) {
  return request<CategoryVO[]>({
    url: `/categories/level/${level}`,
    method: 'get'
  })
}

export function getCategoriesByParentId(parentId: number) {
  return request<CategoryVO[]>({
    url: `/categories/parent/${parentId}`,
    method: 'get'
  })
}

export function createCategory(data: CategoryVO) {
  return request<void>({
    url: '/categories',
    method: 'post',
    data
  })
}

export function updateCategory(data: CategoryVO) {
  return request<void>({
    url: '/categories',
    method: 'put',
    data
  })
}

export function deleteCategory(id: number) {
  return request<void>({
    url: `/categories/${id}`,
    method: 'delete'
  })
} 