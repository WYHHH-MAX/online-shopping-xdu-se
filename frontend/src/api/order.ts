import request from '@/utils/request'
import { PageResult } from '@/types/common'

export interface OrderProduct {
  id: number
  name: string
  image: string
  price: number
  quantity: number
}

export interface Order {
  id: number
  orderNo: string
  status: string
  totalAmount: number
  products: OrderProduct[]
  createdTime?: string
  updatedTime?: string
}

export interface OrderQuery {
  status?: string | null
  page?: number
  size?: number
}

export interface CreateOrderResult {
  orderNo: string
  totalAmount: number
}

/**
 * 获取订单列表
 */
export function getOrders(params: OrderQuery) {
  return request<PageResult<Order>>({
    url: '/orders',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(orderNo: string) {
  return request<Order>({
    url: `/orders/${orderNo}`,
    method: 'get'
  })
}

/**
 * 创建订单
 */
export function createOrder(data: { cartItemIds: number[] }) {
  console.log('调用创建订单API，参数:', data);
  return request<CreateOrderResult>({
    url: '/orders/create',
    method: 'post',
    data
  })
}

/**
 * 支付订单
 */
export function payOrder(orderNo: string) {
  return request({
    url: '/orders/pay',
    method: 'post',
    data: { orderNo }
  })
}

/**
 * 取消订单
 */
export function cancelOrder(orderNo: string) {
  return request({
    url: `/orders/${orderNo}/cancel`,
    method: 'post'
  })
}

/**
 * 确认收货
 */
export function confirmOrder(orderNo: string) {
  return request({
    url: `/orders/${orderNo}/confirm`,
    method: 'post'
  })
} 