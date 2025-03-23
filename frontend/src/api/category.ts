import request from '@/utils/request'
import type { CategoryVO } from '@/types/category'

/**
 * 获取分类树
 */
export function getCategoryTree() {
  return request<{
    code: number;
    message?: string;
    data: any[];
  }>({
    url: '/public/category/tree',
    method: 'get'
  }).then(res => res.data)
}

/**
 * 获取分类列表
 */
export function getCategoryList() {
  return request<{
    code: number;
    message?: string;
    data: any[];
  }>({
    url: '/public/category/list',
    method: 'get'
  }).then(res => res.data)
}

/**
 * 获取分类详情
 */
export function getCategoryById(id: number) {
  return request<{
    code: number;
    message?: string;
    data: any;
  }>({
    url: `/public/category/${id}`,
    method: 'get'
  }).then(res => res.data)
}

/**
 * 根据层级获取分类
 */
export const getCategoriesByLevel = (level: number) => {
  return request<{
    code: number;
    message?: string;
    data: any[];
  }>({
    url: `/public/category/level/${level}`,
    method: 'get'
  }).then(res => res.data)
}

/**
 * 根据父ID获取分类
 */
export const getCategoriesByParentId = (parentId: number) => {
  return request<{
    code: number;
    message?: string;
    data: any[];
  }>({
    url: `/public/category/parent/${parentId}`,
    method: 'get'
  }).then(res => res.data)
}

/**
 * 创建分类
 */
export function createCategory(data: CategoryVO) {
  return request<void>({
    url: '/public/category',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 */
export function updateCategory(data: CategoryVO) {
  return request<void>({
    url: '/public/category',
    method: 'put',
    data
  })
}

/**
 * 删除分类
 */
export const deleteCategory = (id: number) => {
  return request<void>({
    url: `/public/category/${id}`,
    method: 'delete'
  })
} 