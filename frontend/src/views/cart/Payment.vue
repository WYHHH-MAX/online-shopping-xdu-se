<template>
  <div class="payment-page">
    <a-result
      :status="payStatus"
      :title="payStatus === 'success' ? 'Payment Successful' : 'Waiting for Payment'"
      :sub-title="orderNo ? `Order Number: ${orderNo}` : ''"
    >
      <template #extra>
        <div v-if="payStatus !== 'success'" class="payment-container">
          <div class="qr-code-container">
            <h3>{{ payMethodText }} Payment</h3>
            <div class="qr-code-wrapper">
              <img 
                :src="qrCodeUrl" 
                :alt="payMethodText + ' QR Code'" 
                class="payment-qr-code"
              />
              <div class="qr-code-instruction">
                Please scan with {{ payMethodText }} to complete payment
              </div>
            </div>
            
            <div class="order-amount">
              <span>Amount due:</span>
              <span class="amount">¥ {{ orderAmount }}</span>
            </div>
            
            <div class="payment-actions">
              <a-button @click="goBack">Return</a-button>
              <a-button type="primary" @click="simulatePayment">
                Simulate Payment
              </a-button>
            </div>
          </div>
        </div>
        <div v-else class="action-buttons">
          <a-button type="primary" @click="goToOrderDetail">View Order</a-button>
          <a-button @click="goToHome">Return to Homepage</a-button>
        </div>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { payOrder, getOrderDetail } from '../../api/order'
import { getSellerInfo } from '../../api/seller'

const route = useRoute()
const router = useRouter()
const orderNo = ref<string>('')
const orderAmount = ref<number>(0)
const payStatus = ref<'info' | 'success'>('info')
const paymentMethod = ref<string>('')

// QR code URLs
const wechatQrCodeUrl = ref<string>('/images/pay/wechat_1.png')
const alipayQrCodeUrl = ref<string>('/images/pay/alipay_1.png')

// Computed properties
const payMethodText = computed(() => {
  return paymentMethod.value === '2' ? 'WeChat' : 'Alipay'
})

const qrCodeUrl = computed(() => {
  // Build the complete URL with the correct base path
  const basePath = import.meta.env.DEV ? '/api' : ''
  const path = paymentMethod.value === '2' ? wechatQrCodeUrl.value : alipayQrCodeUrl.value
  return `${basePath}${path}`
})

// Get order information when component is mounted
onMounted(async () => {
  // Get order number from route params
  orderNo.value = route.params.orderNo as string
  orderAmount.value = parseFloat(route.query.amount as string) || 0
  paymentMethod.value = route.query.method as string || '1'
  
  if (!orderNo.value) {
    message.error('Order number does not exist')
    router.push('/cart/checkout')
    return
  }
  
  try {
    // Get order details
    const orderDetailResponse = await getOrderDetail(orderNo.value)
    if (orderDetailResponse?.data) {
      // Update amount if not provided in route
      if (!route.query.amount) {
        orderAmount.value = orderDetailResponse.data.totalAmount
      }
      
      // Get seller information including payment QR codes
      try {
        const sellerInfo = await getSellerInfo()
        console.log('Seller information:', sellerInfo)
        
        if (sellerInfo) {
          // Set payment QR code paths - ensure paths are properly formatted
          if (sellerInfo.wechatQrCode) {
            // Remove any duplicate '/api' prefix that might be in the path
            let wechatPath = sellerInfo.wechatQrCode
            wechatPath = wechatPath.replace(/^\/api/, '')
            
            if (paymentMethod.value === '2') {
              wechatQrCodeUrl.value = wechatPath
              console.log('Using WeChat QR code path:', wechatQrCodeUrl.value)
            }
          }
          
          if (sellerInfo.alipayQrCode) {
            // Remove any duplicate '/api' prefix that might be in the path
            let alipayPath = sellerInfo.alipayQrCode
            alipayPath = alipayPath.replace(/^\/api/, '')
            
            if (paymentMethod.value === '1') {
              alipayQrCodeUrl.value = alipayPath
              console.log('Using Alipay QR code path:', alipayQrCodeUrl.value)
            }
          }
        }
      } catch (sellerError) {
        console.error('Failed to get seller information:', sellerError)
      }
    }
  } catch (error: any) {
    message.error(error.message || 'Failed to get order details')
  }
})

// Simulate payment completion
const simulatePayment = async () => {
  try {
    // Show loading message
    message.loading({ content: `Processing ${payMethodText.value} payment...`, duration: 2 })
    
    // Delay 1.5 seconds to simulate payment process
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Call payment API
    const response = await payOrder(orderNo.value, paymentMethod.value)
    
    if (response && (response.code === 1 || response.code === 200)) {
      // Update payment status
      payStatus.value = 'success'
      message.success('Payment successful!')
    } else {
      message.error(response?.msg || 'Payment failed, please try again later')
    }
  } catch (error: any) {
    message.error(error.message || 'Payment failed')
  }
}

// Return to previous page
const goBack = () => {
  router.push('/profile')
}

// Go to order detail
const goToOrderDetail = () => {
  router.push(`/payment-success/${orderNo.value}`)
}

// Return to home page
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

.payment-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 24px 0;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 400px;
}

.qr-code-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid #f0f0f0;
  padding: 20px;
  border-radius: 8px;
  background-color: #fff;
  margin-bottom: 24px;
  width: 100%;
}

.payment-qr-code {
  width: 200px;
  height: 200px;
  object-fit: contain;
  margin-bottom: 16px;
}

.qr-code-instruction {
  font-size: 14px;
  color: #666;
  text-align: center;
}

.order-amount {
  margin: 16px 0;
  font-size: 16px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #f5222d;
}

.payment-actions {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style> 