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
  id?: number
  userId?: number
  shopName?: string
  shopLogo?: string
  logo?: string // 兼容性字段，与shopLogo同义
  description?: string
  shopDesc?: string // 兼容性字段，与description同义
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  businessLicense?: string
  idCardFront?: string
  idCardBack?: string
  status?: number
  wechatQrCode?: string // 微信支付二维码
  alipayQrCode?: string // 支付宝支付二维码
  createTime?: string
  updateTime?: string
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

// 销售数据分析
export interface SalesAnalyticsData {
  // 销售总览数据
  overview: {
    totalSales: number   // 总销售额
    totalOrders: number  // 总订单数
    averageOrderValue: number // 平均订单金额
  }
  
  // 按时间的销售数据
  salesByTime: {
    period: string  // 日期/时间段
    amount: number  // 销售额
    orderCount: number // 订单数
  }[]
  
  // 按商品类别的销售数据
  salesByCategory: {
    category: string  // 商品类别名称
    amount: number    // 销售额
    percentage: number // 销售占比
  }[]
  
  // 热销商品数据
  topProducts: {
    productId: number
    productName: string
    salesAmount: number
    salesCount: number
  }[]
}

// 财务报表请求
export interface FinancialReportRequest {
  startDate: string
  endDate: string
  reportType: 'daily' | 'weekly' | 'monthly' | 'custom'
} 