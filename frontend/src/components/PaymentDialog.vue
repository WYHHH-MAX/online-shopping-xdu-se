<template>
  <a-modal
    v-model:visible="visible"
    title="订单支付"
    :footer="null"
    @cancel="handleCancel"
  >
    <div class="payment-dialog">
      <div class="amount">
        <span class="label">The amount to be paid:</span>
        <span class="value">￥{{ amount }}</span>
      </div>
      
      <div class="payment-methods">
        <h3>Select a payment method</h3>
        <a-radio-group v-model:value="selectedPaymentMethod">
          <a-radio value="1">Alipay</a-radio>
          <a-radio value="2">WeChat Pay</a-radio>
          <a-radio value="3">Bank cards</a-radio>
        </a-radio-group>
      </div>
      
      <div class="actions">
        <a-space>
          <a-button type="primary" :loading="loading" @click="handlePay">
            Confirm the payment
          </a-button>
          <a-button @click="handleCancel">Cancel</a-button>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { payOrder } from '@/api/order'

const props = defineProps<{
  visible: boolean
  orderNo: string
  amount: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}>()

const loading = ref(false)
const selectedPaymentMethod = ref('1') // 默认选择支付宝

const handlePay = async () => {
  loading.value = true
  try {
    // console.log('开始支付订单，订单号:', props.orderNo);
    
    // 确保orderNo是字符串
    const orderNoStr = String(props.orderNo).trim();
    // console.log('处理后的订单号:', orderNoStr);
    
    await payOrder(orderNoStr, selectedPaymentMethod.value)
    message.success('支付成功')
    emit('success')
    emit('update:visible', false)
  } catch (error) {
    console.error('支付失败:', error);
    // message.error('支付失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  emit('update:visible', false)
}
</script>

<style scoped>
.payment-dialog {
  padding: 24px;
  text-align: center;
}

.amount {
  margin-bottom: 24px;
  font-size: 16px;
}

.amount .label {
  color: #666;
}

.amount .value {
  color: #f5222d;
  font-size: 24px;
  font-weight: bold;
}

.payment-methods {
  margin: 24px 0;
  text-align: left;
}

.payment-methods h3 {
  margin-bottom: 12px;
}

.actions {
  margin-top: 24px;
}
</style> 