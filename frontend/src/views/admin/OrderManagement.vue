<template>
  <div class="order-management">
    <a-card class="search-card" title="订单搜索">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="订单号">
          <a-input v-model:value="searchForm.orderNo" placeholder="请输入订单号" allow-clear />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择订单状态" style="width: 150px" allow-clear>
            <a-select-option :value="0">待付款</a-select-option>
            <a-select-option :value="1">待发货</a-select-option>
            <a-select-option :value="2">待收货</a-select-option>
            <a-select-option :value="3">已完成</a-select-option>
            <a-select-option :value="4">已取消</a-select-option>
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
        :data-source="orders"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 订单号 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'orderNo'">
            <a-typography-paragraph copyable>{{ record.orderNo }}</a-typography-paragraph>
          </template>

          <!-- 订单状态 -->
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- 支付方式 -->
          <template v-if="column.dataIndex === 'paymentMethod'">
            <span>{{ getPaymentMethodText(record.paymentMethod) }}</span>
          </template>

          <!-- 创建时间 -->
          <template v-if="column.dataIndex === 'createTime'">
            <span>{{ formatDateTime(record.createTime) }}</span>
          </template>

          <!-- 操作 -->
          <template v-if="column.dataIndex === 'action'">
            <div class="actions">
              <a-button 
                v-if="record.status === 1" 
                type="primary" 
                size="small" 
                @click="handleShipOrder(record.orderNo)"
              >
                <car-outlined />
                发货
              </a-button>
              <a-button 
                size="small" 
                @click="handleViewDetail(record.orderNo)"
              >
                <eye-outlined />
                查看详情
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 订单详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="订单详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered v-if="currentOrder" :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }">
        <a-descriptions-item label="订单号">{{ currentOrder.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <a-tag :color="getStatusColor(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="下单用户">{{ currentOrder.username }}</a-descriptions-item>
        <a-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</a-descriptions-item>
        <a-descriptions-item label="支付方式">{{ getPaymentMethodText(currentOrder.paymentMethod) }}</a-descriptions-item>
        <a-descriptions-item label="下单时间">{{ formatDateTime(currentOrder.createTime) }}</a-descriptions-item>
        <a-descriptions-item label="收货人手机号">{{ currentOrder.phone || '未提供' }}</a-descriptions-item>
        <a-descriptions-item label="收货地址">{{ currentOrder.location || '未提供' }}</a-descriptions-item>
      </a-descriptions>

      <a-divider orientation="left">商品信息</a-divider>
      
      <a-table
        :columns="productColumns"
        :data-source="currentOrder?.products || []"
        :pagination="false"
        v-if="currentOrder"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'image'">
            <a-image
              :width="64"
              :src="getImageUrl(record.image)"
              :fallback="defaultImage"
              alt="商品图片"
            />
          </template>
          <template v-if="column.dataIndex === 'price'">
            ¥{{ record.price }}
          </template>
          <template v-if="column.dataIndex === 'subtotal'">
            ¥{{ (record.price * record.quantity).toFixed(2) }}
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  RedoOutlined,
  EyeOutlined,
  CarOutlined
} from '@ant-design/icons-vue'
import { getAllOrders, getOrderDetail } from '@/api/admin'
import { getImageUrl } from '@/utils/imageUtil'
import { useRequest } from '@/utils/request'
import defaultImage from '@/assets/logo.png'

// 定义API响应类型接口
interface ApiResponse<T> {
  code: number
  msg: string
  data?: T
  list?: any[]
  total?: number
  current?: number
  pageSize?: number
}

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
  userId: number
  username: string
  status: number
  totalAmount: number
  paymentMethod: number | string | null
  createTime: string
  updateTime: string
  products: OrderProduct[]
  phone?: string
  location?: string
}

// 定义表格列
const columns = [
  {
    title: '订单号',
    dataIndex: 'orderNo',
    width: 200,
    ellipsis: true
  },
  {
    title: '用户名',
    dataIndex: 'username',
    width: 120
  },
  {
    title: '订单金额',
    dataIndex: 'totalAmount',
    width: 100,
    sorter: true,
    customRender: ({ text }: { text: number }) => `¥${text}`
  },
  {
    title: '订单状态',
    dataIndex: 'status',
    width: 120,
    filters: [
      { text: '待付款', value: 0 },
      { text: '待发货', value: 1 },
      { text: '待收货', value: 2 },
      { text: '已完成', value: 3 },
      { text: '已取消', value: 4 }
    ]
  },
  {
    title: '支付方式',
    dataIndex: 'paymentMethod',
    width: 120
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
    sorter: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 200
  }
]

// 商品表格列
const productColumns = [
  {
    title: '商品图片',
    dataIndex: 'image',
    width: 100
  },
  {
    title: '商品名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '单价',
    dataIndex: 'price',
    width: 100
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    width: 80
  },
  {
    title: '小计',
    dataIndex: 'subtotal',
    width: 100
  }
]

// 定义搜索表单
const searchForm = reactive({
  orderNo: '',
  username: '',
  status: undefined as number | undefined
})

// 表格数据
const orders = ref<Order[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 详情弹窗
const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

// 获取状态文本
const getStatusText = (status: number | string) => {
  const statusMap: Record<string, string> = {
    '0': '待付款',
    '1': '待发货',
    '2': '待收货',
    '3': '已完成',
    '4': '已取消'
  }
  return statusMap[status.toString()] || '未知状态'
}

// 获取状态颜色
const getStatusColor = (status: number | string) => {
  const statusMap: Record<string, string> = {
    '0': 'orange',
    '1': 'blue',
    '2': 'purple',
    '3': 'green',
    '4': 'red'
  }
  return statusMap[status.toString()] || 'default'
}

// 获取支付方式文本
const getPaymentMethodText = (method: number | string | undefined | null) => {
  if (method === undefined || method === null) return '未设置'
  
  const methodMap: Record<string, string> = {
    '0': '未支付',
    '1': '支付宝',
    '2': '微信支付',
    '3': '银行卡',
    '4': '货到付款'
  }
  try {
    return methodMap[method.toString()] || '其他方式'
  } catch (error) {
    console.error('处理支付方式出错:', method, error)
    return '未知方式'
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return ''
  
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 加载订单数据
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      orderNo: searchForm.orderNo || undefined,
      username: searchForm.username || undefined,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    
    const res = await getAllOrders(params) as ApiResponse<any>
    
    if (res && (res.code === 200 || res.code === 1)) {
      
      // 处理不同的响应数据结构
      if (res.data && typeof res.data === 'object') {
        if (res.data.list && Array.isArray(res.data.list)) {
          orders.value = res.data.list.map(sanitizeOrderData)
          pagination.total = res.data.total || 0
        } else if (res.data.records && Array.isArray(res.data.records)) {
          orders.value = res.data.records.map(sanitizeOrderData)
          pagination.total = res.data.total || 0
        } else if (Array.isArray(res.data)) {
          orders.value = res.data.map(sanitizeOrderData)
          pagination.total = res.data.length
        } else {
          console.log('未知格式，尝试直接使用')
          orders.value = [sanitizeOrderData(res.data)]
          pagination.total = 1
        }
      } else if (res.list && Array.isArray(res.list)) {
        orders.value = res.list.map(sanitizeOrderData)
        pagination.total = res.total || res.list.length
      } else {
        console.error('未能识别响应数据格式:', res)
        orders.value = []
        pagination.total = 0
      }
      
    } else {
      console.error('API响应错误:', res)
      message.error(res.msg || '获取订单列表失败')
      orders.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取订单列表异常:', error)
    orders.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 清理订单数据，确保数据完整性
const sanitizeOrderData = (order: any): Order => {
  if (!order) return {
    id: 0,
    orderNo: '',
    userId: 0,
    username: '未知用户',
    status: 0,
    totalAmount: 0,
    paymentMethod: null,
    createTime: '',
    updateTime: '',
    products: [],
    phone: undefined,
    location: undefined
  }
  
  return {
    id: order.id || 0,
    orderNo: order.orderNo || '',
    userId: order.userId || 0,
    username: order.username || '未知用户',
    status: order.status !== undefined ? order.status : 0,
    totalAmount: order.totalAmount || 0,
    paymentMethod: order.paymentMethod,
    createTime: order.createTime || order.createdTime || '',
    updateTime: order.updateTime || order.updatedTime || '',
    products: Array.isArray(order.products) ? order.products : [],
    phone: order.phone,
    location: order.location
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  loadOrders()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.username = ''
  searchForm.status = undefined
  pagination.current = 1
  loadOrders()
}

// 处理表格变化
const handleTableChange = (pag: any, filters: any, sorter: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  // 处理筛选和排序
  loadOrders()
}

// 查看订单详情
const handleViewDetail = async (orderNo: string) => {
  try {
    loading.value = true
    // 使用管理员API查看订单详情
    const res = await getOrderDetail(orderNo)
    
    if (res && (res.code === 1 || res.code === 200)) {
      const orderData = res.data
      // 确保订单数据格式正确
      if (orderData) {
        currentOrder.value = sanitizeOrderData(orderData)
        detailVisible.value = true
      } else {
        message.error('订单详情获取失败')
      }
    } else {
      message.error(res.msg || '订单详情获取失败')
    }
  } catch (error) {
    console.error('获取订单详情异常:', error)
    message.error('获取订单详情失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 发货处理
const handleShipOrder = (orderNo: string) => {
  Modal.confirm({
    title: '确认发货',
    content: `确定要将订单 ${orderNo} 标记为已发货吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const { request } = useRequest()
        const res = await request<{
          code: number;
          msg?: string;
          data?: boolean;
        }>({
          url: `/admin/orders/${orderNo}/ship`,
          method: 'post'
        })
        
        if (res.code === 1 || res.code === 200) {
          message.success('订单已发货')
          loadOrders()
        } else {
          message.error(res.msg || '发货失败')
        }
      } catch (error) {
        console.error('发货操作异常:', error)
        message.error('发货失败，请稍后再试')
      }
    }
  })
}

// 初始加载
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-management {
  padding: 16px 0;
}

.search-card {
  margin-bottom: 16px;
}

.actions button {
  margin-right: 8px;
}
</style> 