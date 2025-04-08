<template>
  <div class="home">
    <div class="content-wrapper">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="logo-container">
          <img src="@/assets/logo.png" alt="Logo" class="search-logo" />
        </div>
        <div class="search-container">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="Please enter a product name"
            enter-button="Search"
            size="large"
            @search="handleSearch"
          />
        </div>
      </div>

      <!-- 分类导航和轮播图区域 -->
      <div class="category-carousel-container">
        <!-- 分类导航 -->
        <div class="category-nav">
          <a-menu
            v-model:selectedKeys="selectedCategory"
            mode="vertical"
            :style="{ width: '200px' }"
            @mouseenter="handleMenuMouseEnter"
          >
            <template v-for="category in categories" :key="'item-' + category.id">
              <a-menu-item 
                @mouseenter="handleCategoryHover(category)" 
                @click="handleCategoryClick(category)"
              >
                <component :is="getIcon(category.icon)" />
                <span>{{ category.name }}</span>
                <RightOutlined class="submenu-arrow" />
              </a-menu-item>
            </template>
          </a-menu>

          <!-- 子分类弹出层 -->
          <div 
            v-if="showSubMenu && hoveredCategory" 
            class="sub-categories" 
            @mouseleave="handleSubMenuMouseLeave"
          >
            <div class="sub-category-content">
              <template v-if="hoveredCategory.children && hoveredCategory.children.length">
                <div v-for="subCategory in hoveredCategory.children" :key="'sub-' + subCategory.id" class="sub-category-section">
                  <h4 @click="handleCategoryClick(subCategory)">{{ subCategory.name }}</h4>
                  <div class="sub-category-items">
                    <a 
                      v-for="item in subCategory.children" 
                      :key="'item-' + item.id"
                      @click="handleCategoryClick(item)"
                      class="sub-category-link"
                    >
                      {{ item.name }}
                    </a>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- 轮播图区域 -->
        <div class="carousel-section">
          <a-carousel autoplay v-if="featuredProducts.length > 0">
            <div v-for="product in featuredProducts" :key="product.id" class="carousel-item">
              <div class="carousel-content-wrapper">
                <img :src="getImageUrl(product.mainImage)" :alt="product.name" class="carousel-image" @error="handleImageError" />
                <div class="carousel-content">
                  <h3>{{ product.name }}</h3>
                  <p>¥{{ product.price }}</p>
                  <a-button type="primary" @click="viewProduct(product)">Find out more</a-button>
                </div>
              </div>
            </div>
          </a-carousel>
          <div v-else class="carousel-placeholder">
            <a-empty description="There are no recommended products" />
          </div>
        </div>
      </div>

      <!-- 推荐商品展示区域 -->
      <div class="featured-products">
        <h2 class="section-title">Recommended products</h2>
        <div class="featured-grid">
          <div v-for="product in featuredProducts" :key="product.id" class="featured-item">
            <img :src="getImageUrl(product.mainImage)" :alt="product.name" class="featured-image" @error="handleImageError" />
            <div class="featured-content">
              <h3>{{ product.name }}</h3>
              <p class="price">¥{{ product.price }}</p>
              <div class="featured-actions">
                <a-button type="primary" @click="viewProduct(product)">Find out more</a-button>
                <a-button type="link" @click="addToCart(product)">
                  <ShoppingCartOutlined />
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <product-list
        :products="products"
        :total="total"
        @page-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, h } from 'vue'
import { message } from 'ant-design-vue'
import { ShoppingCartOutlined, EyeOutlined, AppstoreOutlined, RightOutlined } from '@ant-design/icons-vue'
import * as Icons from '@ant-design/icons-vue'
import { getCategoryTree } from '../api/category'
import { searchProducts, getFeaturedProducts, getNewProducts, getHotProducts } from '../api/product'
import { quickAddToCart } from '../api/cart'
import type { CategoryVO } from '../types/category'
import type { ProductVO } from '../types/product'
import { useRouter } from 'vue-router'
import { getImageUrl, handleImageError } from '../utils/imageUtil'

const router = useRouter()
const searchKeyword = ref('')
const selectedCategory = ref<string[]>([])
const categories = ref<CategoryVO[]>([])
const products = ref<ProductVO[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const featuredProducts = ref<ProductVO[]>([])
const newProducts = ref<ProductVO[]>([])
const hotProducts = ref<ProductVO[]>([])
const searchQuery = ref('')

// 分类导航相关
const showSubMenu = ref(false)
const hoveredCategory = ref<CategoryVO | null>(null)
const subMenuStyle = ref({
  left: '200px',
  top: '0'
})

// 获取图标组件
const getIcon = (iconName: string | undefined) => {
  if (!iconName) return h(AppstoreOutlined)
  const IconComponent = Icons[iconName as keyof typeof Icons]
  return IconComponent ? h(IconComponent) : h(AppstoreOutlined)
}

// 获取分类树
const loadCategories = async () => {
  try {
    const result = await getCategoryTree()
    categories.value = result
  } catch (error) {
    // console.error('获取分类列表失败:', error)
    message.error('获取分类列表失败')
  }
}

// 加载推荐商品
const loadFeaturedProducts = async () => {
  try {
    const result = await getFeaturedProducts()
    // console.log("推荐商品数据:", result)
    if (result && 'list' in result && Array.isArray(result.list)) {
      featuredProducts.value = result.list
    } else if (Array.isArray(result)) {
      featuredProducts.value = result
    } else {
      // console.error('推荐商品数据格式有误:', result)
      featuredProducts.value = []
    }
  } catch (error) {
    // console.error('获取推荐商品失败:', error)
    message.error('获取推荐商品失败')
  }
}

// 加载新品
const loadNewProducts = async () => {
  try {
    const result = await getNewProducts()
    // console.log("新品数据:", result)
    if (result && 'list' in result && Array.isArray(result.list)) {
      newProducts.value = result.list
    } else if (Array.isArray(result)) {
      newProducts.value = result
    } else {
      // console.error('新品数据格式有误:', result)
      newProducts.value = []
    }
  } catch (error) {
    // console.error('获取新品失败:', error)
    message.error('获取新品失败')
  }
}

// 加载热销商品
const loadHotProducts = async () => {
  try {
    const result = await getHotProducts()
    // console.log("热销商品数据:", result)
    if (result && 'list' in result && Array.isArray(result.list)) {
      hotProducts.value = result.list
    } else if (Array.isArray(result)) {
      hotProducts.value = result
    } else {
      // console.error('热销商品数据格式有误:', result)
      hotProducts.value = []
    }
  } catch (error) {
    // console.error('获取热销商品失败:', error)
    message.error('获取热销商品失败')
  }
}

// 搜索商品
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    // message.warning('请输入搜索关键词')
    return
  }
  
  // 跳转到搜索结果页
  router.push({
    path: '/search',
    query: {
      keyword: searchKeyword.value,
      page: '1',
      sort: 'default'
    }
  })
}

// 处理分类点击
const handleCategoryClick = (category: CategoryVO) => {
  router.push(`/category/${category.id}`)
}

// 处理分类悬停
const handleCategoryHover = (category: CategoryVO) => {
  hoveredCategory.value = category
  showSubMenu.value = true
}

// 处理菜单鼠标进入
const handleMenuMouseEnter = () => {
  if (hoveredCategory.value) {
    showSubMenu.value = true
  }
}

// 处理子菜单鼠标离开
const handleSubMenuMouseLeave = () => {
  showSubMenu.value = false
}

// 翻页
const handlePageChange = (page: number) => {
  currentPage.value = page
  handleSearch()
}

// 添加到购物车
const addToCart = async (product: ProductVO) => {
  try {
    // 使用快速添加API
    await quickAddToCart(product.id)
    message.success('添加到购物车成功')
  } catch (error) {
    // console.error('添加到购物车失败:', error)
    message.error('添加到购物车失败')
  }
}

// 查看商品详情
const viewProduct = (product: ProductVO) => {
  router.push(`/product/${product.id}`)
}

onMounted(() => {
  loadCategories()
  loadFeaturedProducts()
  handleSearch()
})
</script>

<style scoped>
.home {
  height: 100%;
  padding: 24px;
}

.content-wrapper {
  background: #fff;
  padding: 24px;
  min-height: calc(100vh - 64px - 48px);
  border-radius: 4px;
}

.search-bar {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 24px;
}

.logo-container {
  flex: 0 0 200px;
  display: flex;
  align-items: center;
}

.search-logo {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.search-container {
  flex: 1;
  max-width: 600px;
}

.category-carousel-container {
  display: flex;
  margin-bottom: 24px;
  position: relative;
  gap: 24px;
}

.category-nav {
  position: relative;
  z-index: 100;
  flex: 0 0 200px;
}

.category-nav .ant-menu {
  border-right: none;
  height: 100%;
}

.submenu-arrow {
  position: absolute;
  right: 16px;
  font-size: 12px;
}

.sub-categories {
  position: absolute;
  left: 200px;
  width: 400px;
  top: 0;
  bottom: 0;
  overflow-y: auto;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.sub-category-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sub-category-section {
  margin-bottom: 16px;
}

.sub-category-section h4 {
  margin: 0;
  padding: 8px;
  cursor: pointer;
  color: #333;
  transition: all 0.3s;
  font-weight: bold;
  border-bottom: 1px solid #f0f0f0;
}

.sub-category-section h4:hover {
  color: #1890ff;
  background-color: #f5f5f5;
}

.sub-category-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  padding: 0 8px;
}

.sub-category-items a {
  color: #666;
  font-size: 12px;
  padding: 2px 8px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
  transition: all 0.3s;
}

.sub-category-items a:hover {
  color: #1890ff;
  background-color: #f5f5f5;
  border-radius: 2px;
}

.carousel-section {
  flex: 0 0 900px;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.right-section {
  flex: 1;
  min-height: 300px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.carousel-item {
  height: 300px;
  color: #fff;
  text-align: center;
  background: #364d79;
  position: relative;
}

.carousel-content-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  max-height: 100%;
}

/* 添加媒体查询，针对大屏幕优化轮播图 */
@media (min-width: 1440px) {
  .carousel-section {
    max-width: calc(100% - 240px);
    height: 400px;
  }
  
  .carousel-item {
    height: 400px;
  }
  
  .carousel-image {
    object-fit: contain;
    background-color: #f0f0f0;
  }
}

.carousel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  z-index: 10;
  text-align: left;
}

.carousel-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f0f2f5;
}

.featured-products {
  margin: 24px 0;
}

.section-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 4px solid #1890ff;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
  margin-top: 16px;
}

.featured-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.featured-item:hover {
  transform: translateY(-5px);
}

.featured-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.featured-content {
  padding: 16px;
}

.featured-content h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.featured-content .price {
  color: #f5222d;
  font-size: 18px;
  font-weight: bold;
  margin: 8px 0;
}

.featured-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

:deep(.ant-menu-vertical) {
  border-right: none;
}

:deep(.ant-menu-item), :deep(.ant-menu-submenu-title) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.ant-menu-item .anticon), :deep(.ant-menu-submenu-title .anticon) {
  font-size: 16px;
}

.sub-category-link {
  color: #333;
  text-decoration: none;
  padding: 4px 8px;
  display: block;
  transition: all 0.3s;
}

.sub-category-link:hover {
  color: #1890ff;
  background-color: #f5f5f5;
}

.carousel-content h3 {
  color: #fff;
  margin: 0;
  font-size: 24px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.carousel-content p {
  color: #fff;
  margin: 8px 0 16px;
  font-size: 20px;
}

:deep(.slick-slide) {
  pointer-events: auto;
}

:deep(.slick-dots) {
  margin-bottom: 8px;
}

:deep(.slick-dots li button) {
  background: rgba(255, 255, 255, 0.7);
}

:deep(.slick-dots li.slick-active button) {
  background: white;
}

/* 添加媒体查询，针对小屏幕优化布局 */
@media (max-width: 1200px) {
  .category-carousel-container {
    flex-direction: column;
  }
  
  .carousel-section {
    flex: none;
    width: 100%;
  }
  
  .category-nav {
    flex: none;
    width: 100%;
  }
}
</style>