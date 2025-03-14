import request from '@/utils/request'

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
}

export interface OrderQuery {
  status?: string | null
  page?: number
  size?: number
}

export function getOrders(params: OrderQuery) {
  return request({
    url: '/orders',
    method: 'get',
    params
  })
}

export function payOrder(orderNo: string) {
  return request({
    url: `/orders/${orderNo}/pay`,
    method: 'post'
  })
}

export function cancelOrder(orderNo: string) {
  return request({
    url: `/orders/${orderNo}/cancel`,
    method: 'post'
  })
}

export function confirmOrder(orderNo: string) {
  return request({
    url: `/orders/${orderNo}/confirm`,
    method: 'post'
  })
} 