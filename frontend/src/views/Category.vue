<template>
  <div class="category-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <a-breadcrumb>
        <a-breadcrumb-item>
          <router-link to="/">Home</router-link>
        </a-breadcrumb-item>
        <template v-if="parentCategory">
          <a-breadcrumb-item>
            <router-link :to="`/category/${parentCategory.id}`">{{ parentCategory.name }}</router-link>
          </a-breadcrumb-item>
        </template>
        <a-breadcrumb-item>{{ categoryName }}</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <!-- 排序和筛选区域 -->
    <div class="filter-section">
      <a-space>
        <a-select v-model:value="sortBy" style="width: 120px" @change="handleSortChange">
          <a-select-option value="default">Default sorting</a-select-option>
          <a-select-option value="price_asc">Prices range from low to high</a-select-option>
          <a-select-option value="price_desc">Prices go from high to low</a-select-option>
          <a-select-option value="sales_desc">Prioritize sales</a-select-option>
          <a-select-option value="rating_desc">Praise is preferred</a-select-option>
        </a-select>
      </a-space>
    </div>

    <!-- 商品列表 -->
    <div class="product-list">
      <a-row :gutter="[16, 16]">
        <a-col :span="6" v-for="product in products" :key="product.id">
          <div class="product-card" @click="navigateToProduct(product.id)">
            <img :src="getImageUrl(product.mainImage)" :alt="product.name" class="product-image" />
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-price">¥{{ product.price }}</p>
              <p class="product-sales">sales: {{ product.sales }}</p>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <a-pagination
        v-model:current="currentPage"
        :total="total"
        :pageSize="pageSize"
        show-quick-jumper
        show-size-changer
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getProductsByCategory } from '../api/product'
import { getCategoryById } from '../api/category'
import type { ProductVO } from '../types/product'
import type { CategoryVO } from '../types/category'
import { getImageUrl } from '../utils/imageUtil'

interface Result<T> {
  code: number
  message: string
  data: T
}

const route = useRoute()
const router = useRouter()
const categoryId = ref<number>(0)
const categoryName = ref('')
const products = ref<ProductVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const sortBy = ref('default')
const parentCategory = ref<CategoryVO | null>(null)

// 加载分类信息
const loadCategory = async () => {
  try {
    const result = await getCategoryById(Number(route.params.id))
    // console.log('分类信息:', result)
    
    categoryId.value = result.id
    categoryName.value = result.name
    
    // 设置父分类信息
    if (result.parent) {
      parentCategory.value = result.parent
      // console.log('找到父分类:', result.parent.name)
    } else if (result.parentId && result.parentId !== 0) {
      // 如果没有直接提供parent对象但有parentId，尝试加载父分类
      try {
        const parentData = await getCategoryById(result.parentId)
        parentCategory.value = parentData
        // console.log('加载父分类成功:', parentData.name)
      } catch (err) {
        // console.error('加载父分类失败:', err)
      }
    }
  } catch (error) {
    // console.error('获取分类信息失败:', error)
    message.error('获取分类信息失败')
  }
}

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    const result = await getProductsByCategory(
      Number(route.params.id),
      currentPage.value,
      pageSize.value,
      sortBy.value
    )
    
    // console.log("分类商品数据:", result)
    
    // 处理不同的数据格式
    if (result && result.list && Array.isArray(result.list)) {
      products.value = result.list
      total.value = result.total || result.list.length
    } else if (result && Array.isArray(result)) {
      products.value = result
      total.value = result.length
    } else if (result && (result as any).data) {
      // 处理数据在data字段中的情况
      const data = (result as any).data
      if (Array.isArray(data)) {
        products.value = data
        total.value = data.length
      } else if (data.list && Array.isArray(data.list)) {
        products.value = data.list
        total.value = data.total || data.list.length
      } else {
        // console.error('无法解析data字段中的商品数据:', data)
        products.value = []
        total.value = 0
      }
    } else {
      // console.error('分类商品数据格式有误:', result)
      products.value = []
      total.value = 0
    }
  } catch (error) {
    // console.error('获取商品列表失败:', error)
    message.error('获取商品列表失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理排序变化
const handleSortChange = (value: string) => {
  sortBy.value = value
  currentPage.value = 1
  loadProducts()
}

// 处理页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadProducts()
}

// 跳转到商品详情
const navigateToProduct = (productId: number) => {
  router.push(`/product/${productId}`)
}

onMounted(() => {
  loadCategory()
  loadProducts()
})
</script>

<style scoped>
.category-page {
  padding: 24px;
}

.breadcrumb {
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.product-list {
  margin-bottom: 24px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 12px;
}

.product-name {
  margin: 0;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin: 8px 0;
  font-size: 16px;
  color: #f5222d;
  font-weight: bold;
}

.product-sales {
  margin: 4px 0;
  font-size: 12px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style> 