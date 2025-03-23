<template>
  <div class="payment-success">
    <div class="success-container">
      <div class="success-icon">
        <CheckCircleFilled style="font-size: 72px; color: #52c41a;" />
      </div>
      <h1 class="success-title">支付成功</h1>
      <div class="order-info">
        <p class="info-item">
          <span class="label">订单号：</span>
          <span class="value">{{ orderNo }}</span>
        </p>
        <p class="info-item">
          <span class="label">支付金额：</span>
          <span class="value">¥{{ amount }}</span>
        </p>
      </div>
      <div class="actions">
        <a-button type="primary" @click="viewOrder">查看订单</a-button>
        <a-button @click="continueShopping">继续购物</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CheckCircleFilled } from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

const orderNo = ref('')
const amount = ref('')

onMounted(() => {
  // 从路由参数中获取订单信息
  orderNo.value = route.query.orderNo as string || ''
  amount.value = route.query.amount as string || '0'
})

// 查看订单
const viewOrder = () => {
  router.push('/profile')
}

// 继续购物
const continueShopping = () => {
  router.push('/')
}
</script>

<style scoped>
.payment-success {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px);
  padding: 24px;
}

.success-container {
  width: 100%;
  max-width: 600px;
  padding: 48px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.success-icon {
  margin-bottom: 24px;
}

.success-title {
  font-size: 24px;
  color: #52c41a;
  margin-bottom: 32px;
}

.order-info {
  margin-bottom: 32px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
  text-align: left;
}

.info-item {
  margin-bottom: 8px;
}

.label {
  color: #666;
  margin-right: 8px;
}

.value {
  font-weight: 500;
  color: #333;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style> 