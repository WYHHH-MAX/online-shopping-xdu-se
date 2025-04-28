<template>
  <div class="seller-requests">
    <h1 class="page-title">Seller Application Management</h1>
    
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="pending" tab="To be reviewed">
        <a-table
          :columns="columns"
          :data-source="pendingRequests"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          rowKey="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'username'">
              <div class="user-cell">
                <a-avatar :src="record.avatar || ''" size="small" />
                <span class="username">{{ record.username }}</span>
              </div>
            </template>
            
            <template v-if="column.key === 'status'">
              <a-tag color="orange">To be reviewed</a-tag>
            </template>
            
            <template v-if="column.key === 'action'">
              <div class="action-buttons">
                <a-button type="primary" size="small" @click="showDetail(record)">查看</a-button>
                <a-button type="success" size="small" @click="approveRequest(record)">通过</a-button>
                <a-button type="danger" size="small" @click="rejectRequest(record)">拒绝</a-button>
              </div>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
      
      <a-tab-pane key="approved" tab="approved">
        <a-table
          :columns="columns"
          :data-source="approvedRequests"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          rowKey="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'username'">
              <div class="user-cell">
                <a-avatar :src="record.avatar || ''" size="small" />
                <span class="username">{{ record.username }}</span>
              </div>
            </template>
            
            <template v-if="column.key === 'status'">
              <a-tag color="green">Passed</a-tag>
            </template>
            
            <template v-if="column.key === 'action'">
              <a-button type="primary" size="small" @click="showDetail(record)">View</a-button>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
      
      <a-tab-pane key="rejected" tab="rejected">
        <a-table
          :columns="columns"
          :data-source="rejectedRequests"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          rowKey="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'username'">
              <div class="user-cell">
                <a-avatar :src="record.avatar || ''" size="small" />
                <span class="username">{{ record.username }}</span>
              </div>
            </template>
            
            <template v-if="column.key === 'status'">
              <a-tag color="red">Rejected</a-tag>
            </template>
            
            <template v-if="column.key === 'action'">
              <a-button type="primary" size="small" @click="showDetail(record)">check</a-button>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>
    
    <!-- 申请详情对话框 -->
    <a-modal
      v-model:visible="detailVisible"
      title="Application Details"
      width="600px"
      :footer="null"
    >
      <a-descriptions bordered :column="1" v-if="currentRequest">
        <a-descriptions-item label="Application ID">{{ currentRequest.id }}</a-descriptions-item>
        <a-descriptions-item label="Username">{{ currentRequest.username }}</a-descriptions-item>
        <a-descriptions-item label="Contact">{{ currentRequest.phone || 'Not provided' }}</a-descriptions-item>
        <a-descriptions-item label="Email">{{ currentRequest.email || 'Not provided' }}</a-descriptions-item>
        <a-descriptions-item label="Application timeline">{{ currentRequest.applyTime }}</a-descriptions-item>
        <a-descriptions-item label="Reason for application">{{ currentRequest.reason || 'Not provided' }}</a-descriptions-item>
        <a-descriptions-item label="state">
          <a-tag :color="getStatusColor(currentRequest.status)">{{ getStatusText(currentRequest.status) }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>
      
      <div class="modal-footer" v-if="currentRequest && currentRequest.status === 0">
        <a-button type="primary" @click="approveRequest(currentRequest)">Apply by application</a-button>
        <a-button type="danger" @click="rejectRequest(currentRequest)">Rejection of the application</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { getSellerRequests, reviewSellerRequest } from '@/api/admin'
import { useRoute } from 'vue-router'

// 定义申请记录的类型
interface SellerRequest {
  id: number
  userId: number
  username: string
  phone?: string
  email?: string
  avatar?: string
  reason?: string
  status: number // 0-待审核 1-已通过 2-已拒绝
  applyTime: string
  reviewTime?: string
  reviewerId?: number
  reviewReason?: string
}

// 表格列定义
const columns = [
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
  },
  {
    title: '申请时间',
    dataIndex: 'applyTime',
    key: 'applyTime',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
  }
]

// 数据和状态
const allRequests = ref<SellerRequest[]>([])
const loading = ref(false)
const activeTab = ref('pending')
const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

// 详情对话框相关
const detailVisible = ref(false)
const currentRequest = ref<SellerRequest | null>(null)

// 根据tab筛选不同状态的请求
const pendingRequests = computed(() => {
  return allRequests.value.filter(item => item.status === 0)
})

const approvedRequests = computed(() => {
  return allRequests.value.filter(item => item.status === 1)
})

const rejectedRequests = computed(() => {
  return allRequests.value.filter(item => item.status === 2)
})

// 获取状态文字
const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知状态'
  }
}

// 获取状态颜色
const getStatusColor = (status: number) => {
  switch (status) {
    case 0: return 'orange'
    case 1: return 'green'
    case 2: return 'red'
    default: return 'default'
  }
}

const route = useRoute()

// 获取所有申请
const fetchRequests = async () => {
  loading.value = true
  try {
    // console.log('正在获取卖家申请数据，状态:', getStatusByActiveTab())
    
    // 构建查询参数
    const params: any = {
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    
    // 根据当前标签页筛选状态
    const statusValue = getStatusByActiveTab()
    if (statusValue !== '') {
      params.status = parseInt(statusValue)
    }
    
    // console.log('请求参数:', params)
    
    // 发送请求获取卖家申请
    const res: any = await getSellerRequests(params)
    console.log('获取到卖家申请列表原始数据:', res)
    
    // 处理返回的数据
    if (res && typeof res === 'object') {
      // 检查是否有data属性（标准接口返回格式）
      if (res.data && typeof res.data === 'object') {
        // 标准接口返回格式 {code, msg, data: {total, list}}
        if (res.data.list && Array.isArray(res.data.list)) {
          allRequests.value = res.data.list
          pagination.total = res.data.total || 0
          console.log('成功解析标准接口返回格式数据')
        } else if (res.data.records && Array.isArray(res.data.records)) {
          // 兼容另一种分页格式 {code, msg, data: {total, records}}
          allRequests.value = res.data.records
          pagination.total = res.data.total || 0
          console.log('成功解析records格式数据')
        } else if (Array.isArray(res.data)) {
          // data直接是数组
          allRequests.value = res.data
          pagination.total = res.data.length
          console.log('成功解析data数组格式数据')
        } else {
          console.error('无法识别data中的数据格式:', res.data)
          allRequests.value = []
          pagination.total = 0
        }
      } else if (res.records) {
        // 直接返回分页对象格式
        allRequests.value = res.records
        pagination.total = res.total || 0
        console.log('成功解析直接分页对象格式数据')
      } else if (res.list) {
        // 直接返回带list的格式
        allRequests.value = res.list
        pagination.total = res.total || 0
        console.log('成功解析直接list格式数据')
      } else if (Array.isArray(res)) {
        // 直接返回数组
        allRequests.value = res
        pagination.total = res.length
        console.log('成功解析直接数组格式数据')
      } else {
        console.error('未识别的响应格式:', res)
        allRequests.value = []
        pagination.total = 0
      }
    } else {
      console.error('响应不是对象:', res)
      allRequests.value = []
      pagination.total = 0
    }

    console.log('处理后的卖家申请数据:', allRequests.value)
  } catch (error: any) {
    console.error('获取卖家申请失败:', error)
    message.error(error.message || '获取卖家申请失败')
    allRequests.value = []
  } finally {
    loading.value = false
  }
}

// 处理表格变化
const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchRequests()
}

// 根据当前标签页获取状态值
const getStatusByActiveTab = () => {
  switch (activeTab.value) {
    case 'pending': return '0'
    case 'approved': return '1'
    case 'rejected': return '2'
    default: return ''
  }
}

// 当切换标签页时重新加载数据
watch(activeTab, (newValue) => {
  pagination.current = 1
  fetchRequests()
})

// 查看申请详情
const showDetail = (record: SellerRequest) => {
  currentRequest.value = record
  detailVisible.value = true
}

// 通过申请
const approveRequest = async (record: SellerRequest) => {
  try {
    await reviewSellerRequest(record.id, 'approve')
    message.success(`已通过用户 ${record.username} 的卖家申请`)
    
    // 重新加载数据
    fetchRequests()
    
    // 关闭详情对话框
    detailVisible.value = false
  } catch (error: any) {
    console.error('审核申请失败:', error)
    message.error(error.message || '审核申请失败')
  }
}

// 拒绝申请
const rejectRequest = async (record: SellerRequest) => {
  Modal.confirm({
    title: '拒绝申请',
    content: '确定要拒绝用户 ' + record.username + ' 的卖家申请吗？请输入拒绝理由。',
    onOk: async () => {
      // 由于无法在Modal.confirm中直接使用Vue模板，我们使用一个简单的提示输入
      const rejectReason = prompt('请输入拒绝理由：') || ''
      
      try {
        await reviewSellerRequest(record.id, 'reject', 1, rejectReason)
        message.success(`已拒绝用户 ${record.username} 的卖家申请`)
        
        // 重新加载数据
        fetchRequests()
        
        // 关闭详情对话框
        detailVisible.value = false
      } catch (error: any) {
        console.error('审核申请失败:', error)
        message.error(error.message || '审核申请失败')
      }
    }
  })
}

onMounted(() => {
  // 检查URL中是否有申请ID，如果有则直接打开该申请的详情
  const requestId = route.query.id
  if (requestId) {
    // 获取申请详情
    const id = Number(requestId)
    fetchRequests().then(() => {
      const request = allRequests.value.find(item => item.id === id)
      if (request) {
        showDetail(request)
      }
    })
  } else {
    fetchRequests() // 默认加载所有状态的申请
  }
})
</script>

<style scoped>
.seller-requests {
  padding: 24px 0;
}

.page-title {
  margin-bottom: 24px;
  font-size: 24px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  margin-left: 8px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.modal-footer {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 