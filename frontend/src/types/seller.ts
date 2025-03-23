// 商家入驻申请DTO
export interface SellerApplyRequest {
  // 用户注册信息 - 可选，未登录用户需提供
  username?: string
  password?: string
  nickname?: string
  
  // 店铺信息 - 必填
  shopName: string
  description: string
  contactName: string
  contactPhone: string
  contactEmail: string
  businessLicense: string
}

// 商家信息
export interface SellerInfo {
  id: number
  shopName: string
  description: string
  logo: string
  contactName: string
  contactPhone: string
  contactEmail: string
  businessLicense: string
  businessLicenseImage: string
  idCardFront: string
  idCardBack: string
  status: number
  rejectReason: string
  createdTime: string
  updatedTime: string
}

// 商家统计数据
export interface SellerDashboard {
  pendingShipments: number // 待发货订单数
  totalOrders: number // 总订单数
  totalProducts: number // 商品总数
  lowStockProducts: number // 库存不足商品数
}

// 商品DTO
export interface ProductRequest {
  name: string
  description: string
  price: number
  stock: number
  categoryId: number
  mainImage: string
  images?: string[]
}

// 商品库存更新请求
export interface StockUpdateRequest {
  [productId: string]: number
} 