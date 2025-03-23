<template>
  <div class="order-list-page">
    <div class="content-wrapper">
      <h1 class="page-title">我的订单</h1>
      
      <div class="filter-section">
        <a-tabs v-model:activeKey="activeStatus" @change="handleStatusChange">
          <a-tab-pane key="" tab="全部订单"></a-tab-pane>
          <a-tab-pane key="0" tab="待付款"></a-tab-pane>
          <a-tab-pane key="1" tab="待发货"></a-tab-pane>
          <a-tab-pane key="2" tab="待收货"></a-tab-pane>
          <a-tab-pane key="3" tab="已完成"></a-tab-pane>
          <a-tab-pane key="4" tab="已取消"></a-tab-pane>
        </a-tabs>
      </div>
      
      <div v-if="loading" class="loading-container">
        <a-spin tip="加载中..."></a-spin>
      </div>
      
      <div v-else-if="orders.length === 0" class="empty-container">
        <a-empty description="暂无订单"></a-empty>
      </div>
      
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-date">{{ formatDate(order.createTime) }}</span>
            </div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <div class="order-items">
            <div v-for="product in order.products" :key="product.id" class="order-item">
              <img :src="getImageUrl(product.image)" :alt="product.name" class="product-image">
              <div class="product-info">
                <div class="product-name">{{ product.name }}</div>
                <div class="product-price">¥{{ product.price }} × {{ product.quantity }}</div>
              </div>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-amount">
              <span>共 {{ getTotalQuantity(order) }} 件商品，总计：</span>
              <span class="total-price">¥{{ order.totalAmount }}</span>
            </div>
            
            <div class="order-actions">
              <template v-if="order.status === 0">
                <a-button type="primary" @click="handlePayOrder(order)">去支付</a-button>
                <a-button @click="handleCancelOrder(order)">取消订单</a-button>
              </template>
              
              <template v-if="order.status === 2">
                <a-button type="primary" @click="handleConfirmOrder(order)">确认收货</a-button>
              </template>
              
              <a-button v-if="order.status === 3" @click="handleViewOrderDetail(order)">查看详情</a-button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="pagination-container" v-if="orders.length > 0">
        <a-pagination
          v-model:current="currentPage"
          :total="total"
          :pageSize="pageSize"
          show-quick-jumper
          @change="handlePageChange"
        />
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
          <h2>¥{{ currentOrderAmount }}</h2>
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
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { getOrders, payOrder, cancelOrder, confirmOrder, Order } from '@/api/order'
import type { PageResult } from '@/types/common'
import { getImageUrl } from '@/utils/imageUtil'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'

const router = useRouter()

// 订单状态
const ORDER_STATUS = {
  PENDING_PAYMENT: 0,  // 待付款
  PENDING_SHIPMENT: 1, // 待发货
  PENDING_RECEIPT: 2,  // 待收货
  COMPLETED: 3,        // 已完成
  CANCELLED: 4         // 已取消
}

// 列表数据
const orders = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('')

// 支付相关
const paymentModalVisible = ref(false)
const paymentMethod = ref('wechat')
const paymentLoading = ref(false)
const currentOrderNo = ref('')
const currentOrderAmount = ref(0)

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  console.log('开始加载订单列表, 参数:', {
    status: activeStatus.value || null,
    page: currentPage.value,
    size: pageSize.value
  });
  
  try {
    console.log('发送请求前，完整URL:', '/api/orders');
    const result = await getOrders({
      status: activeStatus.value || null,
      page: currentPage.value,
      size: pageSize.value
    })
    
    console.log('订单列表加载成功:', result);
    
    // 处理不同的响应格式
    if (result && result.records) {
      // 原始格式，包含records字段
      orders.value = result.records;
      total.value = result.total || 0;
    } else if (result && result.list) {
      // 包含list字段的格式
      orders.value = result.list;
      total.value = result.total || 0;
    } else if (result && (result as any).data) {
      // 数据在data字段中
      const data = (result as any).data;
      if (Array.isArray(data)) {
        // data直接是数组
        orders.value = data;
        total.value = data.length;
      } else if (data.records) {
        // data.records是数组
        orders.value = data.records;
        total.value = data.total || data.records.length;
      } else if (data.list) {
        // data.list是数组
        orders.value = data.list;
        total.value = data.total || data.list.length;
      } else {
        console.error('订单数据格式异常，data字段结构不符合预期:', data);
        orders.value = [];
        total.value = 0;
      }
    } else if (Array.isArray(result)) {
      // 直接返回数组
      orders.value = result;
      total.value = result.length;
    } else {
      console.warn('API返回的数据格式不符合预期:', result);
      orders.value = [];
      total.value = 0;
    }
    
    console.log('处理后的订单列表数据:', {
      length: orders.value.length,
      total: total.value,
      sample: orders.value.length > 0 ? orders.value[0] : null
    });
  } catch (error: any) {
    console.error('获取订单列表失败:', {
      message: error.message,
      status: error.response?.status,
      data: error.response?.data,
      config: error.config,
      fullURL: error.config?.baseURL ? `${error.config.baseURL}${error.config.url}` : error.config?.url
    });
    
    if (error.response?.status === 404) {
      message.error('订单API路径不存在，请检查后端服务是否正常运行');
    } else {
      message.error(error.message || '获取订单列表失败');
    }
    
    orders.value = [];
    total.value = 0;
  } finally {
    loading.value = false
  }
}

// 获取订单状态文本
const getStatusText = (status: number) => {
  switch (parseInt(status.toString())) {
    case ORDER_STATUS.PENDING_PAYMENT:
      return '待付款'
    case ORDER_STATUS.PENDING_SHIPMENT:
      return '待发货'
    case ORDER_STATUS.PENDING_RECEIPT:
      return '待收货'
    case ORDER_STATUS.COMPLETED:
      return '已完成'
    case ORDER_STATUS.CANCELLED:
      return '已取消'
    default:
      return '未知状态'
  }
}

// 获取订单状态样式类
const getStatusClass = (status: number) => {
  switch (parseInt(status.toString())) {
    case ORDER_STATUS.PENDING_PAYMENT:
      return 'status-pending-payment'
    case ORDER_STATUS.PENDING_SHIPMENT:
      return 'status-pending-shipment'
    case ORDER_STATUS.PENDING_RECEIPT:
      return 'status-pending-receipt'
    case ORDER_STATUS.COMPLETED:
      return 'status-completed'
    case ORDER_STATUS.CANCELLED:
      return 'status-cancelled'
    default:
      return ''
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 计算订单商品总数量
const getTotalQuantity = (order: any) => {
  return order.products.reduce((total: number, product: any) => total + product.quantity, 0)
}

// 切换状态标签
const handleStatusChange = (status: string) => {
  currentPage.value = 1
  loadOrders()
}

// 翻页
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadOrders()
}

// 支付订单
const handlePayOrder = (order: any) => {
  currentOrderNo.value = order.orderNo
  currentOrderAmount.value = order.totalAmount
  paymentModalVisible.value = true
}

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
    
    // 关闭模态框并刷新列表
    paymentModalVisible.value = false
    loadOrders()
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
}

// 取消订单
const handleCancelOrder = (order: any) => {
  Modal.confirm({
    title: '确认取消订单',
    icon: () => h(ExclamationCircleOutlined),
    content: '确定要取消这个订单吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await cancelOrder(order.orderNo)
        message.success('订单已取消')
        loadOrders()
      } catch (error) {
        console.error('取消订单失败:', error)
        message.error('取消订单失败')
      }
    }
  })
}

// 确认收货
const handleConfirmOrder = (order: any) => {
  Modal.confirm({
    title: '确认收货',
    icon: () => h(ExclamationCircleOutlined),
    content: '确认已收到商品吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await confirmOrder(order.orderNo)
        message.success('已确认收货')
        loadOrders()
      } catch (error) {
        console.error('确认收货失败:', error)
        message.error('确认收货失败')
      }
    }
  })
}

// 查看订单详情
const handleViewOrderDetail = (order: any) => {
  router.push(`/order/${order.id}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list-page {
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

.filter-section {
  margin-bottom: 24px;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.order-list {
  margin-bottom: 24px;
}

.order-card {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  margin-bottom: 16px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f5f5;
}

.order-info {
  display: flex;
  gap: 24px;
}

.order-no {
  font-weight: 500;
}

.order-date {
  color: #666;
}

.order-status {
  font-weight: 500;
}

.status-pending-payment {
  color: #faad14;
}

.status-pending-shipment,
.status-pending-receipt {
  color: #1890ff;
}

.status-completed {
  color: #52c41a;
}

.status-cancelled {
  color: #d9d9d9;
}

.order-items {
  padding: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  width: calc(33.33% - 12px);
  min-width: 240px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  margin-right: 12px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #666;
  font-size: 12px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.order-amount {
  color: #666;
}

.total-price {
  font-weight: 500;
  color: #ff4d4f;
  margin-left: 8px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 24px;
  text-align: right;
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