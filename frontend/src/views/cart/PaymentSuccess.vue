<template>
  <div class="payment-success-container">
    <a-result
      status="success"
      title="Payment Successful!"
      sub-title="Your order has been successfully placed and payment has been processed."
    >
      <template #extra>
        <div class="order-info" v-if="orderData">
          <div class="order-number">
            <span class="label">Order Number:</span>
            <span class="value">{{ orderData.orderNo }}</span>
          </div>
          <div class="payment-info">
            <span class="label">Payment Method:</span>
            <span class="value">{{ getPaymentMethodText(orderData.payType) }}</span>
          </div>
          <div class="total-amount">
            <span class="label">Total Amount:</span>
            <span class="value">¥{{ orderData.totalAmount }}</span>
          </div>
          <div class="payment-time">
            <span class="label">Payment Time:</span>
            <span class="value">{{ formatDate(orderData.payTime) }}</span>
          </div>
        </div>
        
        <div class="action-buttons">
          <a-button type="primary" @click="viewOrder">View Order</a-button>
          <a-button @click="continueShopping">Continue Shopping</a-button>
        </div>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getOrderDetail } from '../../api/order'

const route = useRoute()
const router = useRouter()
const orderData = ref<any>(null)

onMounted(async () => {
  const orderNo = route.params.orderNo
  
  if (!orderNo) {
    message.error('Invalid order number')
    router.push('/')
    return
  }
  
  try {
    const response = await getOrderDetail(orderNo as string)
    if (response && response.data) {
      orderData.value = response.data
    } else {
      message.error('Failed to get order details')
    }
  } catch (error: any) {
    message.error(error.message || 'Failed to get order details')
    router.push('/')
  }
})

const getPaymentMethodText = (payType: string | number) => {
  const type = String(payType)
  switch (type) {
    case '1':
      return 'Alipay'
    case '2':
      return 'WeChat Pay'
    case '3':
      return 'Bank Card'
    case '4':
      return 'Cash on Delivery'
    default:
      return 'Unknown'
  }
}

const formatDate = (timestamp: number | string) => {
  if (!timestamp) return '-'
  const date = new Date(Number(timestamp))
  return date.toLocaleString()
}

const viewOrder = () => {
  router.push('/profile')
}

const continueShopping = () => {
  router.push('/')
}
</script>

<style scoped>
.payment-success-container {
  max-width: 800px;
  margin: 32px auto;
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.order-info {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  background-color: #fafafa;
  text-align: left;
  max-width: 500px;
  margin: 0 auto 24px;
}

.order-number, .payment-info, .total-amount, .payment-time {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.payment-time {
  border-bottom: none;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  font-weight: 600;
}

.total-amount .value {
  color: #ff4d4f;
  font-size: 16px;
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style> 