<template>
  <div class="checkout-page">
    <div class="content-wrapper">
      <h1 class="page-title">订单结算</h1>
      
      <div v-if="loading" class="loading-container">
        <a-spin tip="加载中..."></a-spin>
      </div>
      
      <div v-else>
        <div class="section">
          <h2 class="section-title">确认商品信息</h2>
          <div class="product-list">
            <div class="product-header">
              <div class="header-item product">商品信息</div>
              <div class="header-item price">单价</div>
              <div class="header-item quantity">数量</div>
              <div class="header-item subtotal">小计</div>
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
          <h2 class="section-title">订单总计</h2>
          <div class="order-summary">
            <div class="summary-item">
              <span class="item-label">商品总价：</span>
              <span class="item-value">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span class="item-label">商品数量：</span>
              <span class="item-value">{{ totalQuantity }} 件</span>
            </div>
          </div>
          
          <div class="order-total">
            <span class="total-label">应付金额：</span>
            <span class="total-value">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
        
        <div class="actions">
          <a-button @click="goBack">返回购物车</a-button>
          <a-button type="primary" @click="handleSubmitOrder">提交订单</a-button>
        </div>
      </div>
    </div>
    
    <!-- 支付模态框 -->
    <a-modal
      v-model:visible="paymentModalVisible"
      title="订单支付"
      :footer="null"
      @cancel="cancelPayment"
    >
      <div class="payment-modal">
        <div class="payment-amount">
          <p>订单金额</p>
          <h2>¥{{ totalPrice.toFixed(2) }}</h2>
        </div>
        
        <div class="payment-methods">
          <h3>选择支付方式</h3>
          <div class="method-options">
            <a-radio-group v-model:value="paymentMethod">
              <a-radio value="wechat">微信支付</a-radio>
              <a-radio value="alipay">支付宝</a-radio>
              <a-radio value="card">银行卡</a-radio>
            </a-radio-group>
          </div>
        </div>
        
        <div class="payment-actions">
          <a-button @click="cancelPayment">取消</a-button>
          <a-button type="primary" @click="completePayment" :loading="paymentLoading">
            确认支付
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
import { createOrder, payOrder } from '@/api/order'
import type { CartItemVO } from '@/types/cart'
import { getImageUrl } from '@/utils/imageUtil'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const cartItems = ref<CartItemVO[]>([])

// 支付相关
const paymentModalVisible = ref(false)
const paymentMethod = ref('wechat')
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
    console.log('获取购物车商品数据:', result)
    
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
    
    // 获取购物车项ID数组
    const itemIds = cartItems.value.map(item => item.id);
    console.log('准备提交订单，购物车项IDs:', itemIds);
    
    // 检查用户是否已登录
    const userStore = useUserStore();
    if (!userStore.isLoggedIn()) {
      console.error('用户未登录，无法创建订单');
      message.warning('请先登录再提交订单');
      router.push('/login');
      return;
    }
    
    // 显示加载状态
    loading.value = true;
    message.loading({ content: '正在创建订单...', key: 'createOrder' });
    
    // 记录请求和详细路径信息
    const requestData = { cartItemIds: itemIds };
    console.log('发送创建订单请求:', {
      url: '/api/orders/create',
      method: 'POST',
      data: requestData,
    });
    
    // 发送创建订单请求
    try {
      const result = await createOrder({ cartItemIds: itemIds });
      
      // 处理响应
      console.log('创建订单成功，响应:', result);
      currentOrderNo.value = result.orderNo;
      message.success({ content: '订单创建成功!', key: 'createOrder' });
      
      // 跳转到支付页面
      console.log('跳转到支付页面，订单号:', result.orderNo);
      router.push(`/payment/${result.orderNo}`);
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
  if (!currentOrderNo.value) {
    message.error('订单号不存在')
    return
  }
  
  paymentLoading.value = true
  try {
    await payOrder(currentOrderNo.value)
    message.success('支付成功')
    
    // 关闭模态框并跳转到支付成功页面
    paymentModalVisible.value = false
    
    // 跳转到支付成功页面，带上订单号和金额参数
    router.push({
      path: '/payment-success',
      query: {
        orderNo: currentOrderNo.value,
        amount: totalPrice.value.toFixed(2)
      }
    })
  } catch (error) {
    console.error('支付失败:', error)
    message.error('支付失败')
  } finally {
    paymentLoading.value = false
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