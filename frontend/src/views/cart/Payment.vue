<template>
  <div class="payment-page">
    <a-result
      :status="payStatus"
      :title="payStatus === 'success' ? '支付成功' : '等待支付'"
      :sub-title="orderNo ? `订单号: ${orderNo}` : ''"
    >
      <template #extra>
        <div v-if="payStatus !== 'success'" class="payment-methods">
          <h3>请选择支付方式</h3>
          <div class="payment-options">
            <a-button type="primary" size="large" @click="handlePay('wechat')" class="payment-btn wechat-btn">
              <template #icon><WechatOutlined /></template>
              微信支付
            </a-button>
            <a-button type="primary" size="large" @click="handlePay('alipay')" class="payment-btn alipay-btn">
              <template #icon><AlipayOutlined /></template>
              支付宝支付
            </a-button>
          </div>
          <div class="order-amount">
            <span>应付金额：</span>
            <span class="amount">¥ {{ orderAmount }}</span>
          </div>
        </div>
        <div class="action-buttons">
          <a-button v-if="payStatus === 'success'" type="primary" @click="goToOrderList">查看订单</a-button>
          <a-button @click="goToHome">返回首页</a-button>
        </div>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { WechatOutlined, AlipayOutlined } from '@ant-design/icons-vue'
import { payOrder, getOrderDetail } from '../../api/order'

const route = useRoute()
const router = useRouter()
const orderNo = ref<string>('')
const orderAmount = ref<number>(0)
const payStatus = ref<'info' | 'success'>('info')

// 在组件挂载时获取订单信息
onMounted(async () => {
  // 从路由参数获取订单号
  orderNo.value = route.params.orderNo as string
  
  if (!orderNo.value) {
    message.error('订单号不存在')
    router.push('/cart/checkout')
    return
  }
  
  try {
    // 获取订单详情
    const orderDetail = await getOrderDetail(orderNo.value)
    orderAmount.value = orderDetail.totalAmount
  } catch (error: any) {
    console.error('获取订单详情失败:', error)
    message.error(error.message || '获取订单详情失败')
  }
})

// 处理支付
const handlePay = async (payMethod: 'wechat' | 'alipay') => {
  try {
    // 模拟支付延迟
    message.loading({ content: `正在使用${payMethod === 'wechat' ? '微信' : '支付宝'}支付...`, duration: 2 })
    
    // 延迟1.5秒模拟支付过程
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 调用支付API
    await payOrder(orderNo.value)
    
    // 更新支付状态
    payStatus.value = 'success'
    message.success('支付成功！')
  } catch (error: any) {
    console.error('支付失败:', error)
    message.error(error.message || '支付失败')
  }
}

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/user/orders')
}

// 返回首页
const goToHome = () => {
  router.push('/')
}
</script>

<style scoped>
.payment-page {
  max-width: 800px;
  margin: 24px auto;
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.payment-methods {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 24px 0;
}

.payment-options {
  display: flex;
  gap: 24px;
  margin-top: 16px;
}

.payment-btn {
  min-width: 160px;
  height: 50px;
}

.wechat-btn {
  background-color: #07c160;
  border-color: #07c160;
}

.alipay-btn {
  background-color: #1677ff;
  border-color: #1677ff;
}

.order-amount {
  margin-top: 32px;
  font-size: 16px;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #f5222d;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style> 