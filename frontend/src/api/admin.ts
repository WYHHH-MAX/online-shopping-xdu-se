import request from '@/utils/request'

// 获取管理员仪表盘统计数据
export const getAdminStats = () => {
  return request({
    url: '/admin/stats',
    method: 'get'
  })
}

// 获取待处理的卖家申请
export const getPendingSellerRequests = () => {
  return request({
    url: '/admin/seller/pending',
    method: 'get'
  })
}

// 获取所有卖家申请（可根据状态筛选）
export const getSellerRequests = (params: {
  status?: number
  page: number
  pageSize: number
}) => {
  return request({
    url: '/admin/seller/requests',
    method: 'get',
    params
  })
}

// 审批卖家申请 - 通过
export const approveSellerRequest = (id: number, adminId: number = 1) => {
  return request({
    url: `/admin/seller/approve/${id}`,
    method: 'post',
    params: { adminId }
  })
}

// 审批卖家申请 - 拒绝
export const rejectSellerRequest = (id: number, adminId: number = 1, reason: string) => {
  return request({
    url: `/admin/seller/reject/${id}`,
    method: 'post',
    params: { adminId, reason }
  })
}

// 审批卖家申请（兼容旧接口）
export const reviewSellerRequest = (id: number, action: 'approve' | 'reject', adminId: number = 1, rejectReason?: string) => {
  if (action === 'approve') {
    return approveSellerRequest(id, adminId);
  } else {
    return rejectSellerRequest(id, adminId, rejectReason || '未提供拒绝理由');
  }
}

// 获取用户列表
export const getUserList = (params: {
  username?: string
  role?: number
  status?: number
  page: number
  pageSize: number
}) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export const getUserDetail = (id: number) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  })
}

// 更新用户状态（启用/禁用）
export const updateUserStatus = (id: number, status: number) => {
  return request({
    url: `/admin/user/status/${id}`,
    method: 'post',
    params: { status }
  })
}

// 获取商品列表（管理员）
export const getAdminProducts = (params: {
  name?: string
  status?: number
  page: number
  pageSize: number
}) => {
  return request({
    url: '/admin/products',
    method: 'get',
    params
  })
}

// 获取商品详情（管理员）
export const getAdminProductDetail = (id: number) => {
  return request({
    url: `/admin/products/${id}`,
    method: 'get'
  })
}

// 更新商品状态（上架/下架）
export const updateProductStatus = (id: number, status: number) => {
  return request({
    url: `/admin/products/status/${id}`,
    method: 'post',
    params: { status }
  })
}

// 设置商品为推荐/取消推荐
export const setProductFeatured = (id: number, featured: number) => {
  return request({
    url: `/admin/products/featured/${id}`,
    method: 'post',
    params: { featured }
  })
}

// 删除商品
export const deleteProduct = (id: number) => {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有卖家列表
 * @param params 查询参数
 * @returns 卖家列表数据
 */
export function getAllSellers(params?: any) {
  return request<{
    code: number;
    msg?: string;
    message?: string;
    data: {
      total: number;
      list: any[];
    }
  }>({
    url: '/admin/sellers',
    method: 'get',
    params
  })
}

/**
 * 更新卖家状态
 * @param id 卖家ID
 * @param status 状态值
 * @returns 更新结果
 */
export function updateSellerStatus(id: number, status: number) {
  return request<{
    code: number;
    msg?: string;
    message?: string;
    data: boolean;
  }>({
    url: `/admin/sellers/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 删除卖家
 * @param id 卖家ID
 * @returns 删除结果
 */
export function deleteSeller(id: number) {
  return request<{
    code: number;
    msg?: string;
    message?: string;
    data: boolean;
  }>({
    url: `/admin/sellers/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有订单列表
 * @param params 查询参数
 * @returns 订单列表数据
 */
export function getAllOrders(params?: any) {
  return request<{
    code: number;
    msg?: string;
    message?: string;
    data: {
      total: number;
      list: any[];
    }
  }>({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param orderNo 订单号
 * @returns 订单详情数据
 */
export function getOrderDetail(orderNo: string) {
  return request<{
    code: number;
    msg?: string;
    message?: string;
    data: any;
  }>({
    url: `/admin/orders/${orderNo}`,
    method: 'get'
  })
} 