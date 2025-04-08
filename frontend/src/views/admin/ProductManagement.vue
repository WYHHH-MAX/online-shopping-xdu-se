<template>
  <div class="product-management">
    <a-card class="search-card" title="Search criteria for today's orders">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="The title of the product">
          <a-input v-model:value="searchForm.name" placeholder="Enter a title" allow-clear />
        </a-form-item>
        <a-form-item label="status">
          <a-select v-model:value="searchForm.status" placeholder="Please select a status" style="width: 150px" allow-clear>
            <a-select-option :value="0">Removed from shelves</a-select-option>
            <a-select-option :value="1">Available</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">
            <search-outlined />
            Search
          </a-button>
          <a-button style="margin-left: 8px" @click="handleReset">
            <redo-outlined />
            reset
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="products"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 图片 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'mainImage'">
            <a-image
              :width="64"
              :src="getImageUrl(record.mainImage)"
              :fallback="defaultImage"
              alt="Product images"
            />
          </template>

          <!-- 价格 -->
          <template v-if="column.dataIndex === 'price'">
            ¥{{ record.price }}
          </template>

          <!-- 状态 -->
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? 'Available' : 'Removed from shelves' }}
            </a-tag>
          </template>

          <!-- 推荐 -->
          <template v-if="column.dataIndex === 'isFeatured'">
            <a-tag :color="record.isFeatured === 1 ? 'success' : 'default'">
              {{ record.isFeatured === 1 ? 'Recommended' : 'haven\'t be recommended' }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.dataIndex === 'action'">
            <div class="actions">
              <a-button 
                v-if="record.status === 0" 
                type="primary" 
                size="small" 
                @click="handleUpdateStatus(record.id, 1)"
              >
                <check-outlined />
                Shelves
              </a-button>
              <a-button 
                v-else 
                danger 
                size="small" 
                @click="handleUpdateStatus(record.id, 0)"
              >
                <stop-outlined />
                Taken off the shelves
              </a-button>
              <a-button 
                v-if="record.isFeatured === 0"
                type="primary"
                size="small"
                @click="handleSetFeatured(record.id, 1)"
              >
                <star-outlined />
                Set it as a recommendation
              </a-button>
              <a-button 
                v-if="record.isFeatured === 1"
                type="primary"
                size="small"
                @click="handleSetFeatured(record.id, 0)"
              >
                <star-filled />
                Cancel a referral
              </a-button>
              <a-button 
                danger 
                size="small" 
                style="margin-left: 8px" 
                @click="handleDelete(record.id)"
              >
                <delete-outlined />
                Delete
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>
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
  DeleteOutlined,
  StarOutlined,
  StarFilled
} from '@ant-design/icons-vue'
import { getAdminProducts, updateProductStatus, deleteProduct, setProductFeatured } from '@/api/admin'
import { getImageUrl } from '@/utils/imageUtil'
// 导入默认图片
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

interface Product {
  id: number
  name: string
  description: string
  price: number
  stock: number
  salesCount: number
  status: number
  mainImage: string
  sellerId: number
  sellerName: string
  createdTime: string
  updatedTime: string
}

// 定义表格列
const columns = [
  {
    title: '商品图片',
    dataIndex: 'mainImage',
    width: 100
  },
  {
    title: '商品名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '价格',
    dataIndex: 'price',
    width: 100,
    sorter: true
  },
  {
    title: '库存',
    dataIndex: 'stock',
    width: 100
  },
  {
    title: '销量',
    dataIndex: 'salesCount',
    width: 100
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    filters: [
      { text: '已上架', value: 1 },
      { text: '已下架', value: 0 }
    ]
  },
  {
    title: '推荐',
    dataIndex: 'isFeatured',
    width: 100
  },
  {
    title: '卖家',
    dataIndex: 'sellerName',
    ellipsis: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 200
  }
]

// 定义搜索表单
const searchForm = reactive({
  name: '',
  status: undefined as number | undefined
})

// 表格数据
const products = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `total ${total} records`
})

// 加载商品数据
const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      name: searchForm.name || undefined,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    
    // console.log('请求参数:', params)
    const res = await getAdminProducts(params) as ApiResponse<any>
    // console.log('商品列表API响应:', res)
    
    // 更加灵活的响应处理逻辑
    if (res && (res.code === 200 || res.code === 1)) {
      // console.log('响应成功, 数据结构:', res)
      
      if (res.data && typeof res.data === 'object') {
        // 检查是否是分页对象
        if (res.data.list && Array.isArray(res.data.list)) {
          // console.log('标准分页格式 data.list')
          products.value = res.data.list
          pagination.total = res.data.total || 0
        } else if (Array.isArray(res.data)) {
          // console.log('数组格式 data[]')
          products.value = res.data
          pagination.total = res.data.length
        } else {
          console.log('未知格式，尝试直接使用')
          products.value = [res.data]
          pagination.total = 1
        }
      } else if (res.list && Array.isArray(res.list)) {
        // console.log('直接分页格式 list[]')
        products.value = res.list
        pagination.total = res.total || res.list.length
      } else {
        console.error('未能识别响应数据格式:', res)
        products.value = []
        pagination.total = 0
      }
    } else {
      console.error('API响应错误:', res)
      message.error(res.msg || '获取商品列表失败')
      products.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取商品列表异常:', error)
    // message.error('获取商品列表失败，请检查网络连接')
    products.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  loadProducts()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.name = ''
  searchForm.status = undefined
  pagination.current = 1
  loadProducts()
}

// 处理表格变化
const handleTableChange = (pag: any, filters: any, sorter: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  // 处理排序和筛选
  loadProducts()
}

// 修改商品状态
const handleUpdateStatus = async (id: number, status: number) => {
  try {
    const res = await updateProductStatus(id, status) as ApiResponse<boolean>
    if (res && res.code === 1) {
      message.success(`商品已${status === 1 ? '上架' : '下架'}`)
      loadProducts()
    } else {
      message.error(res.msg || '更新商品状态失败')
    }
  } catch (error) {
    console.error('更新商品状态错误:', error)
    message.error('更新商品状态失败，请稍后再试')
  }
}

// 设置商品推荐状态
const handleSetFeatured = async (id: number, featured: number) => {
  try {
    const res = await setProductFeatured(id, featured) as ApiResponse<boolean>
    if (res && res.code === 1) {
      message.success(`商品已${featured === 1 ? '设为推荐' : '取消推荐'}`)
      loadProducts()
    } else {
      message.error(res.msg || '设置商品推荐状态失败')
    }
  } catch (error) {
    console.error('设置商品推荐状态错误:', error)
    message.error('设置商品推荐状态失败，请稍后再试')
  }
}

// 删除商品
const handleDelete = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个商品吗？此操作不可恢复。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteProduct(id) as ApiResponse<boolean>
        if (res && res.code === 1) {
          message.success('商品已删除')
          loadProducts()
        } else {
          message.error(res.msg || '删除商品失败')
        }
      } catch (error) {
        console.error('删除商品错误:', error)
        message.error('删除商品失败，请稍后再试')
      }
    }
  })
}

// 初始加载
onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-management {
  padding: 16px 0;
}

.search-card {
  margin-bottom: 16px;
}

.actions button {
  margin-right: 8px;
}
</style> 