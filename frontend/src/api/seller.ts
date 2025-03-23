import request from '@/utils/request'
import type { ProductRequest, SellerApplyRequest, SellerDashboard, SellerInfo, StockUpdateRequest } from '@/types/seller'
import type { ProductVO } from '@/types/product'
import type { OrderVO } from '@/types/order'

/**
 * 商家入驻申请 - 支持已登录用户和未登录用户
 */
export function applySeller(data: SellerApplyRequest) {
  const isNewUser = !!data.username && !!data.password
  console.log(`提交${isNewUser ? '未登录' : '已登录'}用户的商家入驻申请:`, {
    ...data,
    password: data.password ? '******' : undefined // 日志中隐藏密码
  })
  
  return request<boolean>({
    url: '/seller/apply',
    method: 'post',
    data
  })
  .then(response => {
    console.log("商家入驻申请成功:", response)
    return response
  })
  .catch(error => {
    console.error("商家入驻申请失败:", error)
    // 如果是未登录用户注册失败，提供更具体的错误提示
    if (isNewUser) {
      if (error.response?.data?.message?.includes('用户名已存在')) {
        throw new Error('用户名已存在，请使用其他用户名或直接登录')
      }
    }
    throw error
  })
}

/**
 * 上传商家资质图片
 */
export function uploadQualification(fileType: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request<string>({
    url: `/seller/upload/${fileType}`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取商家信息
 */
export function getSellerInfo() {
  return request<SellerInfo>({
    url: '/seller/info',
    method: 'get'
  })
}

/**
 * 获取商家商品列表
 */
export function getSellerProducts(params: { page?: number, size?: number }) {
  return request<any>({
    url: '/seller/products',
    method: 'get',
    params
  })
}

/**
 * 添加商品
 */
export function addProduct(data: ProductRequest) {
  return request<ProductVO>({
    url: '/seller/products',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 */
export function updateProduct(id: number, data: ProductRequest) {
  return request<ProductVO>({
    url: `/seller/products/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除商品
 */
export function deleteProduct(id: number) {
  return request<boolean>({
    url: `/seller/products/${id}`,
    method: 'delete'
  })
}

/**
 * 上传商品图片
 */
export function uploadProductImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request<string>({
    url: '/seller/products/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取卖家订单列表
 */
export function getSellerOrders(params: { status?: number | string, page?: number, size?: number }) {
  return request<any>({
    url: '/seller/orders',
    method: 'get',
    params
  })
}

/**
 * 发货
 */
export function shipOrder(orderNo: string) {
  return request<boolean>({
    url: `/seller/orders/${orderNo}/ship`,
    method: 'post'
  })
}

/**
 * 获取卖家统计数据
 */
export function getSellerDashboard() {
  return request<SellerDashboard>({
    url: '/seller/dashboard',
    method: 'get'
  })
}

/**
 * 更新商品库存
 */
export function updateProductStock(id: number, stock: number) {
  return request<boolean>({
    url: `/seller/products/${id}/stock`,
    method: 'put',
    params: { stock }
  })
}

/**
 * 批量更新商品库存
 */
export function batchUpdateProductStock(stockMap: StockUpdateRequest) {
  return request<boolean>({
    url: '/seller/products/stock/batch',
    method: 'put',
    data: stockMap
  })
}

/**
 * 更新商家信息
 */
export function updateSeller(data: any) {
  return request<any>({
    url: '/seller/update',
    method: 'put',
    data
  })
} 