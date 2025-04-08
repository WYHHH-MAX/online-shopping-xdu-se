<template>
  <div class="search-results">
    <div class="header">
      <h1 class="search-title">Search results:</h1>
      <div class="search-bar">
        <a-input-search
          v-model:value="keyword"
          placeholder="Please enter a product name"
          enter-button="search"
          size="large"
          @search="handleSearch"
          class="search-input"
        />
      </div>
    </div>

    <div class="results-info">
      <p class="results-count">Totally found <span class="highlight">{{ total }}</span> related products</p>
      <div class="sort-container">
        <span class="sort-label">Sort by:</span>
        <a-select
          v-model:value="sortBy"
          style="width: 140px"
          @change="handleSortChange"
          size="middle"
          class="sort-select"
        >
          <a-select-option value="default">Default sorting</a-select-option>
          <a-select-option value="price_asc">Prices range from low to high</a-select-option>
          <a-select-option value="price_desc">Prices range from high to low</a-select-option>
          <a-select-option value="sales_desc">Prioritize sales</a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 加载提示 -->
    <div v-if="loading" class="loading-container">
      <a-spin tip="Loading..." size="large" />
    </div>
    
    <!-- 商品列表 -->
    <div v-else-if="products.length > 0" class="product-grid">
      <a-card 
        v-for="product in products" 
        :key="product.id" 
        hoverable 
        class="product-card"
        @click="viewProduct(product)"
      >
        <template #cover>
          <div class="image-container">
            <img 
              :src="getImageUrl(product.mainImage)" 
              :alt="product.name" 
              class="product-image" 
            />
          </div>
        </template>
        <a-card-meta :title="product.name" class="product-meta">
          <template #description>
            <div class="product-price">¥{{ product.price }}</div>
            <div class="product-sales">Sales: {{ product.sales }}</div>
          </template>
        </a-card-meta>
        <div class="product-actions">
          <a-button type="primary" size="middle" @click.stop="viewProduct(product)">
            View details
          </a-button>
          <a-button type="link" size="middle" @click.stop="addToCart(product)" class="cart-button">
            <shopping-cart-outlined />
          </a-button>
        </div>
      </a-card>
    </div>
    <a-empty v-else description="The product was not found" class="empty-result" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <a-pagination
        v-model:current="currentPage"
        :total="total"
        :pageSize="pageSize"
        @change="handlePageChange"
        show-quick-jumper
        :showSizeChanger="false"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { ShoppingCartOutlined } from '@ant-design/icons-vue'
import type { ProductVO } from '../types/product'
import { getImageUrl } from '../utils/imageUtil'
import { quickAddToCart } from '../api/cart'
import { searchProducts } from '../api/product'

const router = useRouter()
const route = useRoute()

const keyword = ref('')
const products = ref<ProductVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortBy = ref('default')
const loading = ref(false)

// 加载搜索结果
const loadSearchResults = async () => {
  if (!keyword.value.trim()) return
  
  loading.value = true
  try {
    const result = await searchProducts({
      keyword: keyword.value,
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value !== 'default' ? sortBy.value : undefined
    })
    
    // console.log('搜索结果:', result)
    
    if (result && result.code === 200) {
      const data = result.data
      
      if (data) {
        // 检查data.list是否存在且为数组
        if (data.list && Array.isArray(data.list)) {
          products.value = data.list
          total.value = data.total || data.list.length || 0
        } 
        // 直接检查data是否为数组
        else if (Array.isArray(data)) {
          products.value = data
          total.value = data.length
        } 
        // 如果没有找到有效数据结构，设置为空
        else {
          products.value = []
          total.value = 0
        }
      } else {
        products.value = []
        total.value = 0
      }
      
      // 如果找到数据但total为0，将total设置为列表长度
      if (products.value.length > 0 && total.value === 0) {
        total.value = products.value.length
      }
      
      if (products.value.length === 0 && currentPage.value > 1) {
        // 如果当前页没有数据，但不是第一页，可能是因为删除等原因导致的，返回第一页
        currentPage.value = 1
        updateUrl()
      }
    } else {
      message.error(result?.message || '搜索失败')
      products.value = []
      total.value = 0
    }
  } catch (error) {
    message.error('搜索商品失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  if (!keyword.value.trim()) {
    message.warning('请输入搜索关键词')
    return
  }
  
  // 重置页码并更新URL
  currentPage.value = 1
  updateUrl()
}

// 更新URL，保持搜索状态
const updateUrl = () => {
  router.push({
    path: '/search',
    query: {
      keyword: keyword.value,
      page: currentPage.value.toString(),
      sort: sortBy.value
    }
  })
}

// 处理排序变化
const handleSortChange = () => {
  currentPage.value = 1
  updateUrl()
}

// 处理分页
const handlePageChange = (page: number) => {
  currentPage.value = page
  updateUrl()
}

// 查看商品详情
const viewProduct = (product: ProductVO) => {
  router.push(`/product/${product.id}`)
}

// 添加到购物车
const addToCart = async (product: ProductVO) => {
  try {
    await quickAddToCart(product.id)
    message.success('添加到购物车成功')
  } catch (error) {
    message.error('添加到购物车失败')
  }
}

// 监听路由参数变化 - 放在函数定义后面
watch(
  () => route.query,
  (query) => {
    if (query.keyword) {
      keyword.value = query.keyword as string
    }
    if (query.page) {
      currentPage.value = parseInt(query.page as string) || 1
    }
    if (query.sort) {
      sortBy.value = query.sort as string
    }
    loadSearchResults()
  },
  { immediate: true }
)

onMounted(() => {
  // 如果URL有查询参数，从URL获取
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
    loadSearchResults()
  }
})
</script>

<style scoped>
.search-results {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 200px);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header {
  margin-bottom: 32px;
}

.search-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  border-left: 4px solid #1890ff;
  padding-left: 12px;
}

.search-bar {
  max-width: 600px;
  margin-bottom: 24px;
}

.search-input :deep(.ant-btn) {
  background-color: #1890ff;
  border-color: #1890ff;
}

.results-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f7f7f7;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.results-count {
  margin: 0;
  font-size: 16px;
  color: #555;
}

.highlight {
  color: #1890ff;
  font-weight: bold;
  font-size: 18px;
}

.sort-container {
  display: flex;
  align-items: center;
}

.sort-label {
  margin-right: 8px;
  color: #666;
}

.sort-select :deep(.ant-select-selector) {
  border-radius: 4px;
}

.loading-container {
  margin: 100px auto;
  text-align: center;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.image-container {
  height: 220px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-meta :deep(.ant-card-meta-title) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
}

.product-price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
  margin: 8px 0;
}

.product-sales {
  color: #999;
  font-size: 12px;
}

.product-actions {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-button {
  font-size: 16px;
  padding: 0 8px;
}

.empty-result {
  margin: 80px 0;
}

.pagination {
  text-align: center;
  margin-top: 40px;
  padding-bottom: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-results {
    padding: 16px;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }
  
  .image-container {
    height: 160px;
  }
  
  .product-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .cart-button {
    margin-top: 8px;
  }
  
  .results-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .sort-container {
    width: 100%;
  }
  
  .sort-select {
    width: 100% !important;
  }
}
</style> 