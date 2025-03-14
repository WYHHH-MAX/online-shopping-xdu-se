<template>
  <div class="order-list">
    <a-list
      :data-source="orders"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card :title="'订单号：' + item.orderNo" style="width: 100%">
            <template #extra>
              <a-tag :color="getStatusColor(item.status)">
                {{ getStatusText(item.status) }}
              </a-tag>
            </template>
            
            <!-- 商品列表 -->
            <div class="order-items">
              <div v-for="product in item.products" :key="product.id" class="order-item">
                <img :src="product.image" :alt="product.name" class="product-image" />
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-price">¥{{ product.price.toFixed(2) }} × {{ product.quantity }}</div>
                </div>
              </div>
            </div>
            
            <!-- 订单信息 -->
            <div class="order-info">
              <div class="order-total">
                总计：<span class="price">¥{{ item.totalAmount.toFixed(2) }}</span>
              </div>
              <div class="order-actions">
                <a-button
                  v-if="item.status === 'unpaid'"
                  type="primary"
                  @click="handlePay(item)"
                >
                  立即支付
                </a-button>
                <a-button
                  v-if="item.status === 'unpaid'"
                  @click="handleCancel(item)"
                >
                  取消订单
                </a-button>
                <a-button
                  v-if="item.status === 'shipped'"
                  type="primary"
                  @click="handleConfirm(item)"
                >
                  确认收货
                </a-button>
              </div>
            </div>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getOrders, payOrder, cancelOrder, confirmOrder } from '@/api/order'

const props = defineProps<{
  status: string | null
}>()

interface OrderProduct {
  id: number
  name: string
  image: string
  price: number
  quantity: number
}

interface Order {
  id: number
  orderNo: string
  status: string
  totalAmount: number
  products: OrderProduct[]
}

const orders = ref<Order[]>([])
const loading = ref(false)

const loadOrders = async () => {
  try {
    loading.value = true
    const res = await getOrders({ status: props.status })
    orders.value = res.data
  } catch (error) {
    message.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    unpaid: 'orange',
    paid: 'blue',
    shipped: 'cyan',
    completed: 'green',
    cancelled: 'red'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    unpaid: '待付款',
    paid: '已付款',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || status
}

const handlePay = async (order: Order) => {
  try {
    await payOrder(order.orderNo)
    message.success('支付成功')
    loadOrders()
  } catch (error) {
    message.error('支付失败')
  }
}

const handleCancel = async (order: Order) => {
  try {
    await cancelOrder(order.orderNo)
    message.success('取消订单成功')
    loadOrders()
  } catch (error) {
    message.error('取消订单失败')
  }
}

const handleConfirm = async (order: Order) => {
  try {
    await confirmOrder(order.orderNo)
    message.success('确认收货成功')
    loadOrders()
  } catch (error) {
    message.error('确认收货失败')
  }
}

watch(() => props.status, () => {
  loadOrders()
})

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list {
  margin-top: 16px;
}

.order-items {
  margin: 16px 0;
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
}

.product-info {
  flex: 1;
}

.product-name {
  margin-bottom: 8px;
}

.product-price {
  color: #999;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  font-size: 16px;
}

.price {
  color: #f5222d;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style> 