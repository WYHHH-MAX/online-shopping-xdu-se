<template>
  <div class="seller-management">
    <a-card class="search-card" title="搜索条件">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="店铺名称">
          <a-input v-model:value="searchForm.shopName" placeholder="输入店铺名称" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" style="width: 150px" allow-clear>
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">已拒绝</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">
            <search-outlined />
            搜索
          </a-button>
          <a-button style="margin-left: 8px" @click="handleReset">
            <redo-outlined />
            重置
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="sellers"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 状态 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.dataIndex === 'action'">
            <div class="actions">
              <a-button 
                v-if="record.status === 0" 
                type="primary" 
                size="small" 
                @click="handleApprove(record.id)"
              >
                <check-outlined />
                通过
              </a-button>
              <a-button 
                v-if="record.status === 0" 
                danger 
                size="small" 
                style="margin-left: 8px" 
                @click="handleReject(record.id)"
              >
                <stop-outlined />
                拒绝
              </a-button>
              <a-button 
                v-if="record.status === 1" 
                danger 
                size="small" 
                @click="handleDisable(record.id)"
              >
                <stop-outlined />
                禁用
              </a-button>
              <a-button 
                v-if="record.status === 2" 
                type="primary" 
                size="small" 
                @click="handleEnable(record.id)"
              >
                <check-outlined />
                启用
              </a-button>
              <a-button 
                danger 
                size="small" 
                style="margin-left: 8px" 
                @click="handleDelete(record.id)"
              >
                <delete-outlined />
                删除
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 拒绝理由对话框 -->
    <a-modal v-model:visible="rejectModalVisible" title="拒绝申请" @ok="confirmReject" :confirmLoading="confirmLoading">
      <a-form>
        <a-form-item label="拒绝理由" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
          <a-textarea v-model:value="rejectReason" rows="4" placeholder="请输入拒绝理由" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  RedoOutlined,
  CheckOutlined,
  StopOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { getAllSellers, updateSellerStatus, deleteSeller } from '@/api/admin'

// 定义表格列
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80
  },
  {
    title: '店铺名称',
    dataIndex: 'shopName',
    ellipsis: true
  },
  {
    title: '联系人',
    dataIndex: 'contactName',
    width: 120
  },
  {
    title: '联系电话',
    dataIndex: 'contactPhone',
    width: 150
  },
  {
    title: '联系邮箱',
    dataIndex: 'contactEmail',
    width: 180,
    ellipsis: true
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    filters: [
      { text: '待审核', value: 0 },
      { text: '已通过', value: 1 },
      { text: '已拒绝', value: 2 }
    ]
  },
  {
    title: '创建时间',
    dataIndex: 'createdTime',
    width: 180,
    sorter: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 180
  }
]

// 定义搜索表单
const searchForm = reactive({
  shopName: '',
  status: undefined as number | undefined
})

// 表格数据
const sellers = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 拒绝相关
const rejectModalVisible = ref(false)
const rejectReason = ref('')
const confirmLoading = ref(false)
const currentSellerId = ref<number | null>(null)

// 定义API响应类型
interface ApiResponse<T> {
  code: number;
  msg?: string;
  message?: string;
  data: T;
  list?: any[];
  total?: number;
  current?: number;
  pageSize?: number;
}

// 定义卖家接口
interface Seller {
  id: number;
  userId: number;
  shopName: string;
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
  status: number;
  createdTime?: string;
  updatedTime?: string;
}

// 获取状态文本
const getStatusText = (status: number) => {
  switch(status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知状态'
  }
}

// 获取状态颜色
const getStatusColor = (status: number) => {
  switch(status) {
    case 0: return 'blue'
    case 1: return 'green'
    case 2: return 'red'
    default: return 'default'
  }
}

// 加载卖家数据
const loadSellers = async () => {
  loading.value = true
  try {
    const params = {
      shopName: searchForm.shopName || undefined,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    
    console.log('请求参数:', params)
    const res = await getAllSellers(params) as ApiResponse<any>
    console.log('卖家列表API响应:', res)
    
    if (res.code === 200 || res.code === 1) {
      if (res.data && typeof res.data === 'object') {
        // 检查是否是分页对象
        if (res.data.list && Array.isArray(res.data.list)) {
          sellers.value = res.data.list
          pagination.total = res.data.total || 0
        } else if (Array.isArray(res.data)) {
          sellers.value = res.data
          pagination.total = res.data.length
        } else {
          sellers.value = [res.data]
          pagination.total = 1
        }
      } else if (res.list && Array.isArray(res.list)) {
        sellers.value = res.list
        pagination.total = res.total || res.list.length
      } else {
        message.error('获取卖家列表数据格式错误')
        sellers.value = []
        pagination.total = 0
      }
    } else {
      message.error(res.msg || '获取卖家列表失败')
      sellers.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取卖家列表错误:', error)
    message.error('获取卖家列表失败，请稍后再试')
    sellers.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  loadSellers()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.shopName = ''
  searchForm.status = undefined
  pagination.current = 1
  loadSellers()
}

// 处理表格变化
const handleTableChange = (pag: any, filters: any, sorter: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  // 处理筛选
  if (filters.status && filters.status.length > 0) {
    searchForm.status = filters.status[0]
  } else {
    searchForm.status = undefined
  }
  // 处理排序
  if (sorter && sorter.field) {
    // 在此处可以添加排序逻辑
  }
  
  loadSellers()
}

// 通过申请
const handleApprove = (id: number) => {
  Modal.confirm({
    title: '确认通过',
    content: '确定要通过这个卖家申请吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateSellerStatus(id, 1) as ApiResponse<boolean>;
        if (res.code === 200 || res.code === 1) {
          message.success('卖家申请已通过')
          loadSellers()
        } else {
          message.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('通过卖家申请错误:', error)
        message.error('操作失败，请稍后再试')
      }
    }
  })
}

// 拒绝申请
const handleReject = (id: number) => {
  currentSellerId.value = id
  rejectReason.value = ''
  rejectModalVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    message.warning('请输入拒绝理由')
    return
  }
  
  confirmLoading.value = true
  try {
    const res = await updateSellerStatus(currentSellerId.value as number, 2) as ApiResponse<boolean>;
    if (res.code === 200 || res.code === 1) {
      message.success('已拒绝卖家申请')
      rejectModalVisible.value = false
      loadSellers()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('拒绝卖家申请错误:', error)
    message.error('操作失败，请稍后再试')
  } finally {
    confirmLoading.value = false
  }
}

// 禁用卖家
const handleDisable = (id: number) => {
  Modal.confirm({
    title: '确认禁用',
    content: '确定要禁用这个卖家吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateSellerStatus(id, 2) as ApiResponse<boolean>;
        if (res.code === 200 || res.code === 1) {
          message.success('卖家已禁用')
          loadSellers()
        } else {
          message.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('禁用卖家错误:', error)
        message.error('操作失败，请稍后再试')
      }
    }
  })
}

// 启用卖家
const handleEnable = (id: number) => {
  Modal.confirm({
    title: '确认启用',
    content: '确定要启用这个卖家吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateSellerStatus(id, 1) as ApiResponse<boolean>;
        if (res.code === 200 || res.code === 1) {
          message.success('卖家已启用')
          loadSellers()
        } else {
          message.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('启用卖家错误:', error)
        message.error('操作失败，请稍后再试')
      }
    }
  })
}

// 删除卖家
const handleDelete = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个卖家吗？此操作不可恢复。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteSeller(id) as ApiResponse<boolean>;
        if (res.code === 200 || res.code === 1) {
          message.success('卖家已删除')
          loadSellers()
        } else {
          message.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除卖家错误:', error)
        message.error('删除失败，请稍后再试')
      }
    }
  })
}

// 初始加载
onMounted(() => {
  loadSellers()
})
</script>

<style scoped>
.seller-management {
  padding: 16px 0;
}

.search-card {
  margin-bottom: 16px;
}

.actions button {
  margin-right: 8px;
}
</style> 