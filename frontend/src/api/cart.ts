import request from '@/utils/request'
import { CartItemVO } from '@/types/cart'

// 添加到购物车的参数
interface AddCartItemDTO {
  productId: number;
  quantity: number;
}

// 更新购物车项的参数
interface UpdateCartItemDTO {
  id: number;
  quantity: number;
}

/**
 * 获取购物车商品列表
 */
export function getCartItems() {
  return request<CartItemVO[]>({
    url: '/cart/list',
    method: 'get'
  })
}

/**
 * 添加商品到购物车
 * @param productId 商品ID
 * @param quantity 数量
 */
export function addToCart(productId: number, quantity: number) {
  return request<void>({
    url: '/cart/add',
    method: 'post',
    data: { productId, quantity }
  })
}

/**
 * 快速添加商品到购物车（数量固定为1）
 * @param productId 商品ID
 */
export function quickAddToCart(productId: number) {
  return request<void>({
    url: '/cart/quickAdd',
    method: 'post',
    data: { productId }
  })
}

/**
 * 更新购物车商品数量
 * @param id 购物车项ID
 * @param quantity 数量
 */
export function updateCartItem(id: number, quantity: number) {
  return request<void>({
    url: '/cart/update',
    method: 'put',
    data: { id, quantity }
  })
}

/**
 * 删除购物车商品
 * @param id 购物车项ID
 */
export function deleteCartItem(id: number) {
  return request<void>({
    url: `/cart/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 选中/取消选中购物车商品
 * @param id 购物车项ID
 * @param selected 是否选中
 */
export function selectCartItem(id: number, selected: boolean) {
  return request<void>({
    url: '/cart/select',
    method: 'put',
    data: {
      id,
      selected
    }
  })
}

/**
 * 批量选择购物车项
 * @param ids 购物车项ID数组
 */
export function selectCartItems(ids: number[]) {
  return request<void>({
    url: '/cart/selectBatch',
    method: 'post',
    data: { ids }
  })
}

/**
 * 全选/取消全选购物车商品
 * @param selected 是否全选
 */
export function selectAllCartItems(selected: boolean) {
  return request<void>({
    url: '/cart/selectAll',
    method: 'put',
    data: { selected }
  })
}

/**
 * 获取购物车商品数量
 */
export function getCartCount() {
  return request<number>({
    url: '/cart/count',
    method: 'get'
  })
} 
