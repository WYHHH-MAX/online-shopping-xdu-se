<template>
  <div class="dashboard">
    <h1>卖家数据仪表盘</h1>
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card>
          <template #title>待发货订单</template>
          <div class="stat-value">{{ dashboardData.pendingShipments || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <template #title>总订单数</template>
          <div class="stat-value">{{ dashboardData.totalOrders || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <template #title>产品总数</template>
          <div class="stat-value">{{ dashboardData.totalProducts || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <template #title>库存不足商品</template>
          <div class="stat-value">{{ dashboardData.lowStockProducts || 0 }}</div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSellerDashboard } from '@/api/seller'
import { message } from 'ant-design-vue'

interface DashboardData {
  pendingShipments: number;
  totalOrders: number;
  totalProducts: number;
  lowStockProducts: number;
}

const dashboardData = ref<DashboardData>({
  pendingShipments: 0,
  totalOrders: 0,
  totalProducts: 0,
  lowStockProducts: 0
})

const fetchDashboardData = async () => {
  try {
    const data = await getSellerDashboard()
    dashboardData.value = data
  } catch (error: any) {
    message.error('获取仪表盘数据失败: ' + error.message)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  text-align: center;
}
</style> 