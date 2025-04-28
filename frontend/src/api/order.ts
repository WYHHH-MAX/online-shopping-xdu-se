import request from '@/utils/request'
import { PageResult, ApiResponse } from '@/types/common'

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
  status: string // 后端返回的是字符串表示的数字
  totalAmount: number
  products: OrderProduct[]
  phone?: string
  location?: string
  createdTime?: string
  updatedTime?: string
}

export interface OrderQuery {
  status?: string | null // 传递给后端的状态参数
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
  console.log('调用获取订单列表API, 参数:', params);
  return request<ApiResponse<PageResult<Order>>>({
    url: '/orders',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(orderNo: string) {
  return request<ApiResponse<Order>>({
    url: `/orders/${orderNo}`,
    method: 'get'
  })
}

/**
 * 创建订单
 */
export function createOrder(data: { 
  cartItemIds?: number[], 
  directBuy?: boolean, 
  productId?: number, 
  quantity?: number,
  paymentMethod?: string,  // 添加支付方式参数
  phone: string,           // 添加手机号字段
  location: string         // 添加地址字段
}) {
  console.log('调用创建订单API，参数:', data);
  return request<ApiResponse<CreateOrderResult>>({
    url: '/orders/create',
    method: 'post',
    data
  })
}

/**
 * 支付订单
 */
export function payOrder(orderNo: string, paymentMethod?: string) {
  console.log('正在调用支付订单API，订单号:', orderNo, '支付方式:', paymentMethod);
  
  // 确保orderNo是一个字符串并去除前后空格
  const cleanOrderNo = String(orderNo).trim();
  console.log('处理后的订单号:', cleanOrderNo);
  
  // 确保支付方式是数字字符串
  const payMethod = paymentMethod ? String(paymentMethod).trim() : '1';
  console.log('使用的支付方式:', payMethod);
  
  return request<ApiResponse<any>>({
    url: '/orders/pay',
    method: 'post',
    data: { 
      orderNo: cleanOrderNo,
      paymentMethod: payMethod // 使用指定的支付方式
    }
  }).then(response => {
    console.log('支付订单API成功:', response);
    return response;
  }).catch(error => {
    console.error('支付订单API失败:', error);
    throw error;
  });
}

/**
 * 取消订单
 */
export function cancelOrder(orderNo: string) {
  return request<ApiResponse<any>>({
    url: `/orders/${orderNo}/cancel`,
    method: 'post'
  })
}

/**
 * 申请退款
 */
export function refundOrder(orderNo: string) {
  return request<ApiResponse<any>>({
    url: `/orders/${orderNo}/refund`,
    method: 'post'
  })
}

/**
 * 确认收货
 */
export function confirmOrder(orderNo: string) {
  return request<ApiResponse<any>>({
    url: `/orders/${orderNo}/confirm`,
    method: 'post'
  })
} 