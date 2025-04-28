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
  }).then(response => {
    console.log('上传资质图片响应:', response);
    
    // 从响应中提取URL
    let url = '';
    
    // 如果响应是字符串类型
    if (typeof response === 'string') {
      url = response;
      console.log('1. 从字符串响应中获取URL:', url);
    } 
    // 如果响应是对象类型且包含data字段
    else if (response && typeof response === 'object') {
      if ('data' in response) {
        url = (response as any).data;
        console.log('2. 从响应对象的data字段获取URL:', url);
      } else if ('code' in response && 'data' in response) {
        url = (response as any).data;
        console.log('3. 从标准API响应的data字段获取URL:', url);
      }
    }
    
    // 处理URL中可能存在的重复前缀
    if (url && typeof url === 'string' && url.startsWith('/api/api/')) {
      url = url.replace('/api/api/', '/api/');
      console.log('4. 修正重复前缀后的URL:', url);
    }
    
    console.log('5. 最终返回的URL:', url);
    return url;
  });
}

/**
 * 获取商家信息
 */
export function getSellerInfo() {
  return request<any>({
    url: '/seller/info',
    method: 'get'
  }).then(response => {
    console.log('Raw seller info response:', response);
    
    // 标准化字段名，以便前端正确处理
    const data = response;
    
    // 确保字段名一致性
    if (data) {
      // 如果有description但没有shopDesc，添加shopDesc
      if (data.description && !data.shopDesc) {
        data.shopDesc = data.description;
      } 
      // 如果有shopDesc但没有description，添加description
      else if (data.shopDesc && !data.description) {
        data.description = data.shopDesc;
      }
      
      // 处理logo字段
      if (data.logo && !data.shopLogo) {
        data.shopLogo = data.logo;
      } else if (data.shopLogo && !data.logo) {
        data.logo = data.shopLogo;
      }
      
      // 确保支付二维码路径正确
      if (data.wechatQrCode) {
        console.log('Original WeChat QR Code path:', data.wechatQrCode);
        // 使用相对路径
        if (data.wechatQrCode.startsWith('/api/images')) {
          data.wechatQrCode = data.wechatQrCode.replace('/api', '');
        }
        console.log('Processed WeChat QR Code path:', data.wechatQrCode);
      }
      
      if (data.alipayQrCode) {
        console.log('Original Alipay QR Code path:', data.alipayQrCode);
        // 使用相对路径
        if (data.alipayQrCode.startsWith('/api/images')) {
          data.alipayQrCode = data.alipayQrCode.replace('/api', '');
        }
        console.log('Processed Alipay QR Code path:', data.alipayQrCode);
      }
    }
    
    console.log('标准化后的商家信息:', data);
    return data;
  });
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
  
  // 自动处理图片数据
  const processedData = { ...data };
  
  // 如果有图片但没有设置主图，自动设置第一张为主图
  if ((!processedData.mainImage || processedData.mainImage.trim() === '') 
      && processedData.images && processedData.images.length > 0) {
    processedData.mainImage = processedData.images[0];
    console.log('自动设置主图为第一张图片:', processedData.mainImage);
  }
  
  // 验证必要字段
  if (!processedData.name || !processedData.categoryId || processedData.price === undefined || !processedData.mainImage) {
    console.error('商品数据不完整:', { 
      name: !!processedData.name, 
      categoryId: !!processedData.categoryId, 
      price: processedData.price !== undefined,
      mainImage: !!processedData.mainImage 
    });
    return Promise.reject(new Error('商品数据不完整'));
  }
  
  return request<ProductVO>({
    url: '/seller/products',
    method: 'post',
    data: processedData
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
  
  // 自动处理图片数据
  const processedData = { ...data };
  
  // 如果有图片但没有设置主图，自动设置第一张为主图
  if ((!processedData.mainImage || processedData.mainImage.trim() === '') 
      && processedData.images && processedData.images.length > 0) {
    processedData.mainImage = processedData.images[0];
    console.log('自动设置主图为第一张图片:', processedData.mainImage);
  }
  
  // 验证必要字段
  if (!processedData.name || !processedData.categoryId || processedData.price === undefined || !processedData.mainImage) {
    console.error('商品数据不完整:', { 
      name: !!processedData.name, 
      categoryId: !!processedData.categoryId, 
      price: processedData.price !== undefined,
      mainImage: !!processedData.mainImage 
    });
    return Promise.reject(new Error('商品数据不完整'));
  }
  
  return request<ProductVO>({
    url: `/seller/products/${id}`,
    method: 'put',
    data: processedData
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
export function uploadProductImage(file: File, productId?: number, imageIndex?: number) {
  if (!file) {
    return Promise.reject(new Error('文件不能为空'));
  }
  
  console.log(`上传商品图片: ${file.name}, 大小: ${file.size} 字节, 类型: ${file.type}, 商品ID: ${productId}, 序号: ${imageIndex}`);
  const formData = new FormData();
  formData.append('file', file);
  
  // 添加商品ID和图片序号
  if (productId !== undefined) {
    formData.append('productId', productId.toString());
  }
  
  if (imageIndex !== undefined) {
    formData.append('imageIndex', imageIndex.toString());
  }
  
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
  console.log('更新商家信息，请求数据:', {
    ...data,
    shopLogo: data.shopLogo ? (
      typeof data.shopLogo === 'string' && data.shopLogo.length > 30 ? 
      data.shopLogo.substring(0, 30) + '...' : data.shopLogo
    ) : null
  });
  
  return request<any>({
    url: '/seller/info/update',
    method: 'put',
    data
  }).then(response => {
    console.log('更新商家信息成功:', response);
    return response;
  }).catch(error => {
    console.error('更新商家信息失败:', error);
    throw error;
  });
}

/**
 * 删除商品图片
 */
export function deleteProductImage(productId: number, imageUrl: string) {
  console.log(`删除商品图片: productId=${productId}, imageUrl=${imageUrl}`);
  
  if (!productId || !imageUrl) {
    console.error('删除图片参数无效:', { productId, imageUrl });
    return Promise.reject(new Error('参数无效'));
  }
  
  return request<boolean | any>({
    url: '/api/images/product',
    method: 'delete',
    params: { productId, imageUrl }
  }).then(response => {
    console.log('删除图片响应:', response);
    
    // 处理不同格式的响应
    if (typeof response === 'boolean') {
      return response;
    } else if (response && typeof response === 'object') {
      // 尝试从标准响应格式中提取成功状态
      if ('data' in response && typeof response.data === 'boolean') {
        return response.data;
      } else if ('success' in response && typeof response.success === 'boolean') {
        return response.success;
      } else if ('code' in response && typeof response.code === 'number') {
        return response.code === 200 || response.code === 0;
      }
    }
    
    // 默认返回成功
    console.warn('无法从响应中确定删除状态，默认返回成功');
    return true;
  }).catch(error => {
    console.error('删除图片失败:', error);
    throw error;
  });
}

/**
 * 批量删除商品所有图片
 */
export function deleteAllProductImages(productId: number) {
  console.log(`批量删除商品所有图片: productId=${productId}`);
  
  if (!productId) {
    console.error('批量删除图片参数无效:', { productId });
    return Promise.reject(new Error('商品ID参数无效'));
  }
  
  return request<boolean | any>({
    url: '/api/images/product/all',
    method: 'delete',
    params: { productId }
  }).then(response => {
    console.log('批量删除图片响应:', response);
    
    // 处理不同格式的响应
    if (typeof response === 'boolean') {
      return response;
    } else if (response && typeof response === 'object') {
      // 尝试从标准响应格式中提取成功状态
      if ('data' in response && typeof response.data === 'boolean') {
        return response.data;
      } else if ('success' in response && typeof response.success === 'boolean') {
        return response.success;
      } else if ('code' in response && typeof response.code === 'number') {
        return response.code === 200 || response.code === 0;
      }
    }
    
    // 默认返回成功
    console.warn('无法从响应中确定批量删除状态，默认返回成功');
    return true;
  }).catch(error => {
    console.error('批量删除图片失败:', error);
    throw error;
  });
}

/**
 * 获取销售数据分析
 */
export function getSalesAnalytics(params: { startDate?: string, endDate?: string, period?: string }) {
  console.log('请求销售数据分析，参数:', params);
  
  return request<any>({
    url: '/seller/sales/analytics',
    method: 'get',
    params
  }).then(response => {
    console.log('销售数据分析响应:', response);
    
    // 处理不同响应格式
    if (response && typeof response === 'object') {
      // 标准响应结构 {code, message, data}
      if (response.data && typeof response.data === 'object') {
        return response.data;
      } 
      // 直接返回数据对象
      else if (response.overview !== undefined || 
              response.salesByTime !== undefined ||
              response.salesByCategory !== undefined) {
        return response;
      }
    }
    
    console.warn('销售数据分析格式不符合预期:', response);
    // 返回空数据
    return {
      overview: {
        totalSales: 0,
        totalOrders: 0,
        averageOrderValue: 0
      },
      salesByTime: [],
      salesByCategory: [],
      topProducts: []
    };
  }).catch(error => {
    console.error('获取销售数据分析失败:', error);
    throw error;
  });
}

/**
 * 导出财务报表
 */
export function exportFinancialReport(params: { 
  startDate: string, 
  endDate: string, 
  reportType: string
}) {
  console.log('导出财务报表，参数:', params);
  
  return request<Blob>({
    url: '/seller/financial/export',
    method: 'get',
    params,
    responseType: 'blob'  // 指定响应类型为二进制数据
  }).then(response => {
    console.log('导出报表响应类型:', response.type);
    return response;
  }).catch(error => {
    console.error('导出财务报表失败:', error);
    throw error;
  });
}

/**
 * 上传支付二维码
 */
export function uploadPaymentQrCode(payType: 'wechat' | 'alipay', file: File) {
  console.log(`上传${payType}支付二维码, 文件: ${file.name}, 大小: ${file.size} 字节`);
  
  if (!file) {
    return Promise.reject(new Error('文件不能为空'));
  }
  
  const formData = new FormData();
  formData.append('file', file);
  formData.append('payType', payType);
  
  return request<string>({
    url: '/seller/upload/payment-qrcode',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(response => {
    console.log(`${payType}支付二维码上传成功, 响应:`, response);
    
    // 处理不同返回格式
    if (typeof response === 'string') {
      return response;
    } else if (response && typeof response === 'object' && 'data' in response) {
      return (response as any).data as string;
    }
    
    throw new Error('无法获取上传的图片URL');
  }).catch(error => {
    console.error(`${payType}支付二维码上传失败:`, error);
    throw error;
  });
} 