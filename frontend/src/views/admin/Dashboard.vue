<template>
  <div class="admin-dashboard">
    <a-row :gutter="24">
      <a-col :span="24">
        <h1 class="page-title">Administrator Console</h1>
      </a-col>
    </a-row>
    
    <a-row :gutter="24" class="stats-cards">
      <a-col :span="6">
        <a-card hoverable @click="navigateTo('users')">
          <template #cover>
            <div class="stats-icon users">
              <user-outlined />
            </div>
          </template>
          <a-card-meta>
            <template #title>Manage users</template>
            <template #description>
              <div class="stats-value">{{ stats.userCount }}</div>
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
      
<!--      &lt;!&ndash; 注释掉卖家管理卡片-->
<!--      <a-col :span="6">-->
<!--        <a-card hoverable @click="navigateTo('users')">-->
<!--          <template #cover>-->
<!--            <div class="stats-icon sellers">-->
<!--              <shop-outlined />-->
<!--            </div>-->
<!--          </template>-->
<!--          <a-card-meta>-->
<!--            <template #title>管理卖家</template>-->
<!--            <template #description>-->
<!--              <div class="stats-value">{{ stats.sellerCount }}</div>-->
<!--            </template>-->
<!--          </a-card-meta>-->
<!--        </a-card>-->
<!--      </a-col>-->
<!--      &ndash;&gt;-->
      
      <a-col :span="6">
        <a-card hoverable @click="navigateTo('seller-requests')">
          <template #cover>
            <div class="stats-icon pending">
              <solution-outlined />
            </div>
          </template>
          <a-card-meta>
            <template #title>Applications are pending</template>
            <template #description>
              <div class="stats-value">{{ stats.pendingRequests }}</div>
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card hoverable @click="navigateTo('products')">
          <template #cover>
            <div class="stats-icon products">
              <shopping-outlined />
            </div>
          </template>
          <a-card-meta>
            <template #title>Manage products</template>
            <template #description>
              <div class="stats-value">{{ stats.productCount }}</div>
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card hoverable @click="navigateTo('orders')">
          <template #cover>
            <div class="stats-icon orders">
              <shopping-cart-outlined />
            </div>
          </template>
          <a-card-meta>
            <template #title>Order management</template>
            <template #description>
              <div class="stats-value">88</div>
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="24" class="action-cards">
      <a-col :span="8">
        <a-card title="Pending seller application" :bodyStyle="{ height: '300px', overflow: 'auto' }">
          <a-list
            v-if="pendingSellerRequests.length > 0"
            :data-source="pendingSellerRequests"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.username"
                  :description="`Application timeline: ${item.applyTime}`"
                >
                  <template #avatar>
                    <a-avatar :src="item.avatar || '@/assets/avatar.png'" />
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="primary" size="small" @click="reviewSellerRequest(item)">
                    View
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-else description="There are no pending applications at this time" />
        </a-card>
      </a-col>
      
      <a-col :span="16">
        <a-card title="System Overview">
          <a-descriptions :column="2">
            <a-descriptions-item label="System version">v1.0.0</a-descriptions-item>
            <a-descriptions-item label="Last updated">{{ new Date().toLocaleDateString() }}</a-descriptions-item>
            <a-descriptions-item label="System status">Normal operation</a-descriptions-item>
            <a-descriptions-item label="Number of administrators">1</a-descriptions-item>
          </a-descriptions>
          
          <a-divider />
          
          <a-row :gutter="16">
            <a-col :span="12">
              <a-statistic
                title="Today's visits"
                :value="1234"
                style="margin-right: 50px"
              >
                <template #suffix>
                  <rise-outlined style="color: #3f8600" />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="12">
              <a-statistic
                title="Number of orders for today"
                :value="88"
                style="margin-right: 50px"
              >
                <template #suffix>
                  <rise-outlined style="color: #3f8600" />
                </template>
              </a-statistic>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { 
  UserOutlined, 
  ShopOutlined, 
  SolutionOutlined, 
  ShoppingOutlined,
  RiseOutlined,
  ShoppingCartOutlined
} from '@ant-design/icons-vue'
import { getAdminStats, getPendingSellerRequests } from '@/api/admin'
import { useRouter } from 'vue-router'

// Define interfaces for our data
interface SellerRequest {
  id: number
  username: string
  avatar: string
  applyTime: string
  status: number
}

const router = useRouter()

// 统计数据
const stats = ref({
  userCount: 0,
  sellerCount: 0,
  pendingRequests: 0,
  productCount: 0
})

// 待处理的卖家申请
const pendingSellerRequests = ref<SellerRequest[]>([])
const loading = ref(false)

// 获取统计数据
const fetchStats = async () => {
  try {
    // console.log('正在获取管理员统计数据...');
    const res = await getAdminStats() as any
    // console.log('获取到统计数据:', res)
    
    // 确保我们从返回的对象中获取数据，处理不同的响应格式
    if (res && typeof res === 'object') {
      if (res.data && typeof res.data === 'object') {
        // 如果响应有data字段，使用data
        stats.value = {
          userCount: res.data.userCount || 0,
          sellerCount: res.data.sellerCount || 0,
          pendingRequests: res.data.pendingRequests || 0,
          productCount: res.data.productCount || 0
        }
      } else {
        // 直接从响应对象获取数据
        stats.value = {
          userCount: res.userCount || 0,
          sellerCount: res.sellerCount || 0,
          pendingRequests: res.pendingRequests || 0,
          productCount: res.productCount || 0
        }
      }
    }
    
    // console.log('处理后的统计数据:', stats.value);
  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    message.error(error.message || '获取统计数据失败')
    
    // 设置默认值以避免UI显示不正确
    stats.value = {
      userCount: 0,
      sellerCount: 0,
      pendingRequests: 0,
      productCount: 0
    }
  }
}

// 获取待处理的卖家申请
const fetchPendingSellerRequests = async () => {
  loading.value = true
  try {
    // console.log('正在获取待处理的卖家申请...');
    const res: any = await getPendingSellerRequests()
    // console.log('获取到待处理的卖家申请原始数据:', res)
    
    // 处理可能的数据结构差异
    let requests = [];
    
    if (res && typeof res === 'object') {
      // 如果是标准的结果包装类
      if (res.data && (Array.isArray(res.data) || typeof res.data === 'object')) {
        if (Array.isArray(res.data)) {
          requests = res.data;
        } else if (res.data.list && Array.isArray(res.data.list)) {
          requests = res.data.list;
        } else if (res.data.records && Array.isArray(res.data.records)) {
          requests = res.data.records;
        }
      } 
      // 直接返回的列表
      else if (Array.isArray(res)) {
        requests = res;
      } 
      // 分页结果对象
      else if (res.list && Array.isArray(res.list)) {
        requests = res.list;
      } else if (res.records && Array.isArray(res.records)) {
        requests = res.records;
      }
    }
    
    pendingSellerRequests.value = requests;
    // console.log('处理后的待处理申请:', pendingSellerRequests.value);
  } catch (error: any) {
    // console.error('获取待处理的卖家申请失败:', error)
    message.error(error.message || '获取待处理的卖家申请失败')
    pendingSellerRequests.value = []
  } finally {
    loading.value = false
  }
}

// 查看卖家申请详情
const reviewSellerRequest = (item: SellerRequest) => {
  router.push(`/admin/seller-requests?id=${item.id}`)
}

// 跳转到对应管理页面
const navigateTo = (path: string) => {
  router.push(`/admin/${path}`)
}

onMounted(() => {
  fetchStats()
  fetchPendingSellerRequests()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 24px 0;
}

.page-title {
  margin-bottom: 24px;
  font-size: 24px;
}

.stats-cards {
  margin-bottom: 24px;
}

.stats-cards .ant-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stats-cards .ant-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stats-icon {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: white;
}

.stats-icon.users {
  background-color: #1890ff;
}

.stats-icon.sellers {
  background-color: #52c41a;
}

.stats-icon.pending {
  background-color: #faad14;
}

.stats-icon.products {
  background-color: #722ed1;
}

.stats-icon.orders {
  background-color: #f5222d;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.action-cards {
  margin-bottom: 24px;
}
</style> 