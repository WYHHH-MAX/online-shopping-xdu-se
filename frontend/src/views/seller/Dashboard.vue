<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>Seller Data Dashboard</h1>
      <a-button type="primary" @click="goToHome" class="home-button">
        <HomeOutlined />
        Return to the homepage of the mall
      </a-button>
    </div>
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card>
          <template #title>Orders to be shipped</template>
          <div class="stat-value">{{ dashboardData.pendingShipments || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <template #title>Total number of orders</template>
          <div class="stat-value">{{ dashboardData.totalOrders || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <template #title>The total number of products</template>
          <div class="stat-value">{{ dashboardData.totalProducts || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <template #title>Out-of-stock items</template>
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
import { useRouter } from 'vue-router'
import { HomeOutlined } from '@ant-design/icons-vue'

const router = useRouter()

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
    // console.log('正在获取卖家仪表盘数据...');
    const response = await getSellerDashboard();
    // console.log('获取到的原始仪表盘数据:', response);
    
    if (response && typeof response === 'object') {
      // 处理标准响应格式
      if (response.data && typeof response.data === 'object') {
        dashboardData.value = {
          pendingShipments: response.data.pendingShipments || 0,
          totalOrders: response.data.totalOrders || 0,
          totalProducts: response.data.totalProducts || 0,
          lowStockProducts: response.data.lowStockProducts || 0
        };
      } else {
        // 直接使用响应数据
        dashboardData.value = {
          pendingShipments: response.pendingShipments || 0,
          totalOrders: response.totalOrders || 0,
          totalProducts: response.totalProducts || 0,
          lowStockProducts: response.lowStockProducts || 0
        };
      }
      // console.log('处理后的仪表盘数据:', dashboardData.value);
    } else {
      // console.error('仪表盘数据格式不正确:', response);
      message.error('获取仪表盘数据失败: 数据格式不正确');
    }
  } catch (error: any) {
    // console.error('获取仪表盘数据失败:', error);
    message.error('获取仪表盘数据失败: ' + error.message);
  }
}

// 返回商城首页
const goToHome = () => {
  router.push('/');
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.home-button {
  display: flex;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  text-align: center;
}
</style> 