<template>
  <div class="checkout-page">
    <div class="content-wrapper">
      <h1 class="page-title">Order settlement</h1>
      
      <div v-if="loading" class="loading-container">
        <a-spin tip="Loading..."></a-spin>
      </div>
      
      <div v-else>
        <div class="section">
          <h2 class="section-title">Confirm the product information</h2>
          <div class="product-list">
            <div class="product-header">
              <div class="header-item product">Product information</div>
              <div class="header-item price">unit price</div>
              <div class="header-item quantity">quantity</div>
              <div class="header-item subtotal">subtotal</div>
            </div>
            <div v-for="item in cartItems" :key="item.id" class="product-item">
              <div class="item-product">
                <img :src="getImageUrl(item.productImage)" :alt="item.productName" class="product-image">
                <div class="product-info">
                  <div class="product-name">{{ item.productName }}</div>
                </div>
              </div>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-quantity">{{ item.quantity }}</div>
              <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>
        
        <div class="section">
          <h2 class="section-title">Total orders</h2>
          <div class="order-summary">
            <div class="summary-item">
              <span class="item-label">The total price of the item:</span>
              <span class="item-value">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span class="item-label">The number of units:</span>
              <span class="item-value">{{ totalQuantity }} 件</span>
            </div>
          </div>
          
          <div class="order-total">
            <span class="total-label">Amount due:</span>
            <span class="total-value">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
        
        <div class="section">
          <h2 class="section-title">Shipping Information</h2>
          <div class="shipping-info">
            <a-form :model="shippingInfo" layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="Phone Number" name="phone" :rules="[{ required: true, message: 'Please enter your phone number' }]">
                    <a-input v-model:value="shippingInfo.phone" placeholder="Enter your phone number" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="Shipping Address" name="location" :rules="[{ required: true, message: 'Please enter your shipping address' }]">
                    <a-textarea v-model:value="shippingInfo.location" placeholder="Enter your shipping address" :rows="3" />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>
        </div>
        
        <div class="actions">
          <a-button @click="goBack">Return to cart</a-button>
          <a-button type="primary" @click="handleSubmitOrder">Submit your order</a-button>
        </div>
      </div>
    </div>
    
    <!-- 支付模态框 -->
    <a-modal
      v-model:visible="paymentModalVisible"
      title="Order Payment"
      :footer="null"
      @cancel="cancelPayment"
    >
      <div class="payment-modal">
        <div class="payment-amount">
          <p>The amount of the order</p>
          <h2>¥{{ totalPrice.toFixed(2) }}</h2>
        </div>
        
        <div class="payment-methods">
          <h3>Select a payment method</h3>
          <div class="method-options">
            <a-radio-group v-model:value="paymentMethod">
              <a-radio value="1">Alipay</a-radio>
              <a-radio value="2">WeChat Pay</a-radio>
              <a-radio value="3">Bank cards</a-radio>
              <a-radio value="4">cash on delivery</a-radio>
            </a-radio-group>
          </div>
        </div>
        
        <div class="payment-actions">
          <a-button @click="cancelPayment">cancel</a-button>
          <a-button type="primary" @click="completePayment" :loading="paymentLoading">
            Confirm the payment
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getCartItems } from '@/api/cart'
import { getProductDetail } from '@/api/product'
import { createOrder, payOrder } from '@/api/order'
import type { CartItemVO } from '@/types/cart'
import { getImageUrl } from '@/utils/imageUtil'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const cartItems = ref<CartItemVO[]>([])

// 收货信息
const shippingInfo = ref({
  phone: '',
  location: ''
})

// 支付相关
const paymentModalVisible = ref(false)
const paymentMethod = ref('1') // 默认选择支付宝
const paymentLoading = ref(false)
const currentOrderNo = ref('')

// 计算属性
const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0)
})

const totalQuantity = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + item.quantity
  }, 0)
})

// 加载选中的商品
const loadSelectedItems = async () => {
  loading.value = true
  try {
    // 检查是否是直接购买
    const directBuy = route.query.directBuy === 'true'
    const productId = route.query.productId as string
    const productQuantity = route.query.quantity as string
    
    if (directBuy && productId && productQuantity) {
      // 处理直接购买的情况
      // console.log('直接购买模式，商品ID:', productId, '数量:', productQuantity)
      
      try {
        // 这里应该调用API获取商品详情
        const productDetail = await getProductDetail(parseInt(productId))
        
        // 构造购物车项
        cartItems.value = [{
          id: parseInt(productId), // 这里使用商品ID作为临时的购物车项ID
          userId: useUserStore().userId || 0,
          productId: parseInt(productId),
          productName: productDetail.name || '未知商品',
          productImage: productDetail.mainImage || '',
          price: productDetail.price || 0,
          quantity: parseInt(productQuantity),
          selected: true,
          stock: productDetail.stock || 0,
          createTime: new Date().toISOString(),
          updateTime: new Date().toISOString()
        }]
        
        // console.log('直接购买的商品信息:', cartItems.value)
      } catch (error) {
        // console.error('获取商品详情失败:', error)
        message.error('获取商品详情失败')
        router.push('/')
        return
      }
    } else {
      // 处理从购物车结算的情况
      const itemsQuery = route.query.items as string
      if (!itemsQuery) {
        message.error('未选择商品，无法结算')
        router.push('/cart')
        return
      }
      
      const selectedIds = itemsQuery.split(',').map(id => parseInt(id))
      if (selectedIds.length === 0) {
        message.error('未选择商品，无法结算')
        router.push('/cart')
        return
      }
      
      // 获取购物车商品
      const result = await getCartItems()
      // console.log('获取购物车商品数据:', result)
      
      // 处理不同的数据格式
      let items: CartItemVO[] = []
      
      if (result && Array.isArray(result)) {
        // 如果直接返回数组
        items = result
      } else if (result && (result as any).data && Array.isArray((result as any).data)) {
        // 如果数据在data字段中
        items = (result as any).data
      } else if (result && (result as any).list && Array.isArray((result as any).list)) {
        // 如果数据在list字段中
        items = (result as any).list
      } else {
        console.error('购物车数据格式不正确:', result)
        message.error('获取购物车数据格式有误')
        router.push('/cart')
        return
      }
      
      // 筛选选中的商品
      cartItems.value = items.filter(item => selectedIds.includes(item.id))
      
      if (cartItems.value.length === 0) {
        message.error('未找到选中的商品')
        router.push('/cart')
      }
    }
  } catch (error) {
    console.error('加载结算商品失败:', error)
    message.error('加载结算商品失败')
    router.push('/cart')
  } finally {
    loading.value = false
  }
}

// 提交订单
const handleSubmitOrder = async () => {
  try {
    // 确保购物车中有商品
    if (!cartItems.value || cartItems.value.length === 0) {
      message.error('购物车为空，请先添加商品');
      return;
    }
    
    // 验证收货信息
    if (!shippingInfo.value.phone) {
      message.error('请填写收货人手机号');
      return;
    }
    
    if (!shippingInfo.value.location) {
      message.error('请填写收货地址');
      return;
    }
    
    // 检查是否是直接购买模式
    const directBuy = route.query.directBuy === 'true'
    
    // 获取购物车项ID数组或直接购买信息
    let requestData: any;
    
    if (directBuy) {
      // 直接购买模式
      const productId = parseInt(route.query.productId as string);
      const quantity = parseInt(route.query.quantity as string);
      
      requestData = { 
        directBuy: true,
        productId: productId,
        quantity: quantity,
        paymentMethod: paymentMethod.value,
        phone: shippingInfo.value.phone,
        location: shippingInfo.value.location
      };
      console.log('直接购买模式，参数:', requestData);
    } else {
      // 购物车模式
      const itemIds = cartItems.value.map(item => item.id);
      requestData = { 
        cartItemIds: itemIds,
        paymentMethod: paymentMethod.value,
        phone: shippingInfo.value.phone,
        location: shippingInfo.value.location
      };
      console.log('购物车模式，购物车项IDs:', itemIds);
    }
    
    // 检查用户是否已登录
    const userStore = useUserStore();
    if (!userStore.isLoggedIn()) {
      // console.error('用户未登录，无法创建订单');
      message.warning('请先登录再提交订单');
      router.push('/login');
      return;
    }
    
    // 显示加载状态
    loading.value = true;
    message.loading({ content: '正在创建订单...', key: 'createOrder' });
    
    // 发送创建订单请求
    try {
      const response = await createOrder(requestData);
      
      // 处理响应
      // console.log('创建订单成功，响应:', response);
      
      // 解析响应获取orderNo - 根据实际后端返回结构调整
      let orderNo = '';
      
      // 安全获取嵌套属性
      const getNestedProperty = (obj: any, path: string): any => {
        if (!obj) return undefined;
        const keys = path.split('.');
        return keys.reduce((o, key) => o && typeof o === 'object' ? o[key] : undefined, obj);
      };
      
      // 尝试不同的路径获取orderNo
      orderNo = getNestedProperty(response, 'orderNo') || 
               getNestedProperty(response, 'data.orderNo') || '';
      
      if (!orderNo) {
        // console.error('无法从响应中获取订单号:', response);
        message.error('创建订单成功，但无法获取订单号');
        return;
      }
      
      // 保存订单号
      currentOrderNo.value = orderNo;
      // console.log('成功获取到订单号:', orderNo);
      message.success({ content: '订单创建成功!', key: 'createOrder' });
      
      // 显示支付模态框
      // console.log('显示支付模态框，订单号:', orderNo);
      paymentModalVisible.value = true;
    } catch (requestError: any) {
      // 详细记录API请求错误
      console.error('API请求失败:', {
        message: requestError.message,
        response: requestError.response?.data,
        status: requestError.response?.status,
        config: requestError.config,
        fullURL: requestError.config?.baseURL ? `${requestError.config.baseURL}${requestError.config.url}` : requestError.config?.url
      });
      
      if (requestError.response?.status === 404) {
        message.error({
          content: 'API路径不存在，请检查后端服务是否正常运行',
          key: 'createOrder'
        });
        console.error('404错误 - API路径可能不正确，请检查:',
          '/api/orders/create 是否存在，或后端context-path配置是否正确');
      } else {
        message.error({
          content: requestError.response?.data?.message || '创建订单失败，请重试',
          key: 'createOrder'
        });
      }
    }
  } catch (error: any) {
    // 详细记录错误
    console.error('创建订单失败:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      originalError: error
    });
    
    // 根据错误类型显示不同消息
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message.error('订单参数有误，请重试');
          break;
        case 401:
          message.error('登录已过期，请重新登录');
          break;
        case 500:
          message.error('服务器错误，请稍后重试');
          break;
        default:
          message.error(error.response.data?.message || '创建订单失败');
      }
    } else if (error.request) {
      message.error('网络请求失败，请检查网络连接');
    } else {
      message.error(error.message || '创建订单失败');
    }
  } finally {
    loading.value = false;
  }
};

// 完成支付
const completePayment = async () => {
  // 检查订单号是否存在
  if (!currentOrderNo.value) {
    message.error('订单号不存在，请重新提交订单')
    paymentModalVisible.value = false
    return
  }
  
  // 关闭支付模态框
  paymentModalVisible.value = false
  
  // 检查支付方式
  if (paymentMethod.value === '1' || paymentMethod.value === '2') {
    // 支付宝或微信支付 - 跳转到支付二维码页面
    router.push({
      path: `/payment/${currentOrderNo.value}`,
      query: {
        amount: totalPrice.value.toFixed(2),
        method: paymentMethod.value
      }
    })
  } else {
    // 其他支付方式 - 直接处理支付并跳转到成功页面
    paymentLoading.value = true
    try {
      // 确保订单号有效
      const orderNoStr = String(currentOrderNo.value).trim();
      
      // 执行支付操作
      await payOrder(orderNoStr, paymentMethod.value)
      
      // 支付成功
      message.success('支付成功！')
      
      // 跳转到支付成功页面
      router.push({
        path: `/payment-success/${orderNoStr}`,
      })
    } catch (error: any) {
      // 详细记录错误信息
      console.error('支付失败:', {
        orderNo: currentOrderNo.value,
        错误详情: error,
        响应状态: error.response?.status,
        响应数据: error.response?.data,
        请求配置: error.config
      });
      
      // 提供更详细的错误信息
      if (error.response) {
        if (error.response.status === 404) {
          message.error('订单不存在，请重新提交订单')
        } else if (error.response.status === 400) {
          message.error(error.response.data?.message || '订单状态异常，无法支付')
        } else {
          message.error(error.response.data?.message || '支付失败，请稍后重试')
        }
      } else if (error.request) {
        message.error('网络请求失败，请检查网络连接')
      } else {
        message.error(error.message || '支付操作出现异常')
      }
    } finally {
      paymentLoading.value = false
    }
  }
}

// 取消支付
const cancelPayment = () => {
  paymentModalVisible.value = false
  // 可以选择跳转到订单列表页
  router.push('/orders')
}

// 返回购物车
const goBack = () => {
  router.push('/cart')
}

onMounted(() => {
  loadSelectedItems()
})
</script>

<style scoped>
.checkout-page {
  padding: 24px;
}

.content-wrapper {
  background: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.page-title {
  margin-bottom: 24px;
  font-weight: 500;
  color: #333;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.product-list {
  margin-bottom: 24px;
}

.product-header {
  display: flex;
  background-color: #f5f5f5;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  font-weight: 500;
}

.header-item {
  text-align: center;
}

.header-item.product {
  flex: 1;
  text-align: left;
}

.header-item.price,
.header-item.quantity,
.header-item.subtotal {
  flex: 0 0 120px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.item-product {
  flex: 1;
  display: flex;
  align-items: center;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  margin-right: 16px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  color: #333;
}

.item-price,
.item-quantity,
.item-subtotal {
  flex: 0 0 120px;
  text-align: center;
}

.item-subtotal {
  font-weight: 500;
  color: #ff4d4f;
}

.order-summary {
  background-color: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.item-label {
  color: #666;
}

.item-value {
  font-weight: 500;
}

.order-total {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px;
  background-color: #fff7e6;
  border-radius: 4px;
}

.total-label {
  margin-right: 16px;
  font-size: 16px;
}

.total-value {
  font-size: 24px;
  font-weight: 500;
  color: #ff4d4f;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 24px;
}

/* 支付模态框样式 */
.payment-modal {
  padding: 16px;
}

.payment-amount {
  text-align: center;
  margin-bottom: 24px;
}

.payment-amount p {
  color: #666;
  margin-bottom: 8px;
}

.payment-amount h2 {
  font-size: 24px;
  color: #ff4d4f;
}

.payment-methods {
  margin-bottom: 24px;
}

.payment-methods h3 {
  margin-bottom: 16px;
}

.method-options {
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.payment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}
</style> 