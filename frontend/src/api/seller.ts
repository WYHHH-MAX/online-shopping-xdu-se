import request from '@/utils/request'
import type { ProductRequest, SellerApplyRequest, SellerDashboard, SellerInfo, StockUpdateRequest } from '@/types/seller'
import type { ProductVO } from '@/types/product'
import type { OrderVO, OrderProductVO } from '@/types/order'

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
  console.log('请求商家商品列表，参数:', params);
  
  return request<any>({
    url: '/seller/products',
    method: 'get',
    params
  }).then(response => {
    console.log('商品API响应原始数据:', response);
    
    // 确保response存在且包含正确的数据结构
    if (response && typeof response === 'object') {
      let productList = [];
      let total = 0;
      
      // 处理标准响应结构 {code, message, data: {list/records, total}}
      if (response.data && typeof response.data === 'object') {
        if (response.data.list && Array.isArray(response.data.list)) {
          productList = response.data.list;
          total = response.data.total || 0;
          console.log(`从response.data.list获取商品数据，数量: ${productList.length}`);
        } else if (response.data.records && Array.isArray(response.data.records)) {
          productList = response.data.records;
          total = response.data.total || 0;
          console.log(`从response.data.records获取商品数据，数量: ${productList.length}`);
        } 
      } 
      // 处理直接返回的分页结果
      else if (response.list && Array.isArray(response.list)) {
        productList = response.list;
        total = response.total || 0;
        console.log(`从response.list获取商品数据，数量: ${productList.length}`);
      } else if (response.records && Array.isArray(response.records)) {
        productList = response.records;
        total = response.total || 0;
        console.log(`从response.records获取商品数据，数量: ${productList.length}`);
      }
      
      return {
        list: productList,
        total: total
      };
    }
    
    // 处理异常情况
    console.warn('无法解析的商品数据:', response);
    return {
      list: [],
      total: 0
    };
  }).catch(error => {
    console.error('获取商品列表失败:', error);
    // 重新抛出错误，让调用者处理
    throw error;
  });
}

/**
 * 添加商品
 */
export function addProduct(data: ProductRequest) {
  console.log('添加商品API请求数据:', {
    ...data,
    mainImage: data.mainImage ? data.mainImage.substring(0, 20) + '...' : null,
    images: data.images ? `[${data.images.length}个图片]` : null
  });
  
  // 验证必要字段
  if (!data.name || !data.categoryId || data.price === undefined || !data.mainImage) {
    return Promise.reject(new Error('商品数据不完整'));
  }
  
  return request<ProductVO>({
    url: '/seller/products',
    method: 'post',
    data
  }).then(response => {
    console.log('添加商品成功:', response);
    return response;
  }).catch(error => {
    console.error('添加商品失败:', error);
    throw error;
  });
}

/**
 * 更新商品
 */
export function updateProduct(id: number, data: ProductRequest) {
  console.log(`更新商品API请求，ID: ${id}, 数据:`, {
    ...data,
    mainImage: data.mainImage ? data.mainImage.substring(0, 20) + '...' : null,
    images: data.images ? `[${data.images.length}个图片]` : null
  });
  
  if (!id || id <= 0) {
    return Promise.reject(new Error('无效的商品ID'));
  }
  
  // 验证必要字段
  if (!data.name || !data.categoryId || data.price === undefined || !data.mainImage) {
    return Promise.reject(new Error('商品数据不完整'));
  }
  
  return request<ProductVO>({
    url: `/seller/products/${id}`,
    method: 'put',
    data
  }).then(response => {
    console.log('更新商品成功:', response);
    return response;
  }).catch(error => {
    console.error(`更新商品失败, ID: ${id}:`, error);
    throw error;
  });
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
  if (!file) {
    return Promise.reject(new Error('文件不能为空'));
  }
  
  console.log(`上传商品图片: ${file.name}, 大小: ${file.size} 字节, 类型: ${file.type}`);
  const formData = new FormData();
  formData.append('file', file);
  
  return request<string | { code?: number; message?: string; data?: string }>({
    url: '/seller/products/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(response => {
    console.log('图片上传成功, 返回值类型:', typeof response, '返回值:', response);
    // 确保返回的是字符串类型
    if (typeof response === 'string') {
      return response;
    } else if (response && typeof response === 'object') {
      // 处理可能的响应格式 {code: 200, data: "url"}
      if (response.data && typeof response.data === 'string') {
        return response.data;
      }
    }
    // 如果无法获取有效URL，抛出错误
    throw new Error('无法获取上传的图片URL');
  }).catch(error => {
    console.error('图片上传失败:', error);
    throw error;
  });
}

/**
 * 获取卖家订单列表
 */
export function getSellerOrders(params: { status?: number | string, page?: number, size?: number }) {
  console.log('请求卖家订单列表，参数:', params);
  
  return request<{
    code: number;
    message: string;
    data: {
      records?: OrderVO[];
      list?: OrderVO[];
      total: number;
      size: number;
      current: number;
      pages: number;
    }
  }>({
    url: '/seller/orders',
    method: 'get',
    params
  }).then(response => {
    console.log('卖家订单API响应原始数据:', response);
    
    // 确保response.data存在且包含正确的数据结构
    if (response && response.data) {
      let orderList: OrderVO[] = [];
      let total = 0;
      
      // 检查响应数据格式，兼容不同的数据结构
      if (response.data.list) {
        orderList = response.data.list;
        total = response.data.total || 0;
        console.log('从list字段获取订单数据, 订单数量:', orderList.length);
      } else if (response.data.records) {
        orderList = response.data.records;
        total = response.data.total || 0;
        console.log('从records字段获取订单数据, 订单数量:', orderList.length);
      }
      
      // 检查每个订单的数据是否完整
      orderList.forEach((order, index) => {
        if (!order.orderNo) {
          console.warn(`订单[${index}]缺少orderNo字段:`, order);
        }
        if (!order.createTime) {
          console.warn(`订单[${index}]缺少createTime字段:`, order);
        }
        if (order.status === undefined || order.status === null) {
          console.warn(`订单[${index}]缺少status字段:`, order);
        }
      });
      
      return {
        data: {
          list: orderList,
          total: total
        }
      };
    }
    
    console.warn('无法解析的订单数据:', response);
    return {
      data: {
        list: [],
        total: 0
      }
    };
  });
}

/**
 * 发货
 */
export function shipOrder(orderNo: string) {
  console.log(`发起发货请求，订单号: ${orderNo}`);
  
  if (!orderNo) {
    console.error('发货失败: 订单号不能为空');
    return Promise.reject(new Error('订单号不能为空'));
  }
  
  return request<{
    code?: number;
    message?: string;
    data?: boolean;
  } | boolean>({
    url: `/seller/orders/${orderNo}/ship`,
    method: 'post'
  }).then(response => {
    console.log(`发货响应结果:`, response);
    return response;
  }).catch(error => {
    console.error(`发货请求失败:`, error);
    throw error;
  });
}

/**
 * 获取卖家统计数据
 */
export function getSellerDashboard() {
  console.log('请求卖家仪表盘数据');
  
  return request<any>({
    url: '/seller/dashboard',
    method: 'get'
  }).then(response => {
    console.log('获取仪表盘数据成功:', response);
    
    // 处理不同响应格式
    if (response && typeof response === 'object') {
      // 标准响应结构 {code, message, data}
      if (response.data && typeof response.data === 'object') {
        return response.data;
      } 
      // 直接返回数据对象
      else if (response.pendingShipments !== undefined || 
              response.totalOrders !== undefined ||
              response.totalProducts !== undefined) {
        return response;
      }
    }
    
    console.warn('仪表盘数据格式不符合预期:', response);
    // 返回默认值
    return {
      pendingShipments: 0,
      totalOrders: 0,
      totalProducts: 0,
      lowStockProducts: 0
    };
  }).catch(error => {
    console.error('获取仪表盘数据失败:', error);
    throw error;
  });
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
    url: '/seller/info/update',
    method: 'put',
    data
  })
} 