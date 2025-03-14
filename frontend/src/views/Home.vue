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
            placeholder="请输入商品名称"
            enter-button="搜索"
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
              <a-menu-item @mouseenter="() => handleCategoryHover(category)">
                <component :is="getIcon(category.icon)" />
                <span>{{ category.name }}</span>
                <RightOutlined class="submenu-arrow" />
              </a-menu-item>
            </template>
          </a-menu>

          <!-- 子分类弹出层 -->
          <div v-if="showSubMenu && hoveredCategory" class="sub-categories" :style="subMenuStyle">
            <div class="sub-category-content">
              <template v-if="hoveredCategory.children && hoveredCategory.children.length">
                <div v-for="subCategory in hoveredCategory.children" :key="'sub-' + subCategory.id" class="sub-category-section">
                  <h4>{{ subCategory.name }}</h4>
                  <div class="sub-category-items">
                    <a 
                      v-for="item in subCategory.children" 
                      :key="'item-' + item.id"
                      @click.stop="handleSubCategoryClick(item)"
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
                <img :src="getImageUrl(product.mainImage)" :alt="product.name" class="carousel-image" />
                <div class="carousel-content">
                  <h3>{{ product.name }}</h3>
                  <p>¥{{ product.price }}</p>
                  <a-button type="primary" @click="viewProduct(product)">查看详情</a-button>
                </div>
              </div>
            </div>
          </a-carousel>
          <div v-else class="carousel-placeholder">
            <a-empty description="暂无推荐商品" />
          </div>
        </div>
      </div>

      <!-- 推荐商品展示区域 -->
      <div class="featured-products">
        <h2 class="section-title">推荐商品</h2>
        <div class="featured-grid">
          <div v-for="product in featuredProducts" :key="product.id" class="featured-item">
            <img :src="getImageUrl(product.mainImage)" :alt="product.name" class="featured-image" />
            <div class="featured-content">
              <h3>{{ product.name }}</h3>
              <p class="price">¥{{ product.price }}</p>
              <div class="featured-actions">
                <a-button type="primary" @click="viewProduct(product)">查看详情</a-button>
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
import { getCategoryTree } from '@/api/category'
import { searchProducts, getFeaturedProducts } from '@/api/product'
import type { CategoryVO } from '@/types/category'
import type { ProductVO } from '@/types/product'
import { useRouter } from 'vue-router'
import { getImageUrl } from '@/utils/imageUtil'

const router = useRouter()
const searchKeyword = ref('')
const selectedCategory = ref<string[]>([])
const categories = ref<CategoryVO[]>([])
const products = ref<ProductVO[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const featuredProducts = ref<ProductVO[]>([])

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

// 定义API响应类型
interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

interface PageResult<T> {
  total: number;
  list: T[];
}

// 获取分类树
const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    // console.log('分类树响应:', res)
    // 根据Result<List<CategoryVO>>格式处理响应
    const responseData = res.data as any;
    if (responseData && responseData.code === 200) {
      // Result包装的数据在data.data中
      categories.value = responseData.data || []
    } else {
      // console.warn('获取分类数据格式不符合预期:', responseData)
      categories.value = []
    }
  } catch (error) {
    // console.error('获取分类失败:', error)
    message.error('获取分类失败')
  }
}

// 加载推荐商品
const loadFeaturedProducts = async () => {
  try {
    console.log('开始请求推荐商品...')
    console.log('请求URL:', '/api/product/featured', '参数:', { page: 1, size: 5 })
    const res = await getFeaturedProducts()
    console.log('推荐商品响应状态:', res.status)
    console.log('推荐商品响应头:', res.headers)
    console.log('推荐商品原始响应:', res)
    
    // 根据PageResult<ProductVO>格式处理响应
    const responseData = res.data as any;
    console.log('响应数据结构:', responseData)
    console.log('响应数据类型:', typeof responseData)
    
    if (responseData) {
      // 如果是直接返回的PageResult
      if (responseData.list) {
        console.log('使用PageResult.list格式数据')
        featuredProducts.value = responseData.list || []
        console.log('处理后的推荐商品数量:', featuredProducts.value.length)
        // 打印第一个商品的图片URL
        if (featuredProducts.value.length > 0) {
          for (let i = 0; i < Math.min(featuredProducts.value.length, 3); i++) {
            const product = featuredProducts.value[i]
            console.log(`商品${i+1}:`, {
              id: product.id,
              name: product.name,
              mainImage: product.mainImage,
              处理后的URL: getImageUrl(product.mainImage)
            })
          }
          
          // 渲染后检查DOM
          setTimeout(() => {
            console.log('轮播图容器:', document.querySelector('.carousel-section'))
            console.log('轮播图项目数量:', document.querySelectorAll('.carousel-item').length)
            const imgs = document.querySelectorAll('.carousel-image')
            console.log('轮播图图片数量:', imgs.length)
            if (imgs.length > 0) {
              console.log('第一张图片src:', (imgs[0] as HTMLImageElement).src)
              console.log('第一张图片complete:', (imgs[0] as HTMLImageElement).complete)
            }
          }, 1000)
        } else {
          console.warn('没有推荐商品数据')
        }
      } 
      // 如果是Result包装的PageResult
      else if (responseData.code === 200 && responseData.data && responseData.data.list) {
        console.log('使用Result<PageResult>.data.list格式数据')
        featuredProducts.value = responseData.data.list || []
        console.log('处理后的推荐商品:', featuredProducts.value)
      }
      // 如果直接返回数组
      else if (Array.isArray(responseData)) {
        console.log('使用数组格式数据')
        featuredProducts.value = responseData
        console.log('处理后的推荐商品:', featuredProducts.value)
      }
      else {
        console.warn('获取推荐商品数据格式不符合预期:', responseData)
        featuredProducts.value = []
      }
    }
  } catch (error: any) {
    console.error('获取推荐商品失败:', error)
    console.error('错误详情:', {
      status: error?.response?.status,
      data: error?.response?.data,
      message: error?.message
    })
    message.error('获取推荐商品失败')
  }
}

// 搜索商品
const handleSearch = async () => {
  try {
    const res = await searchProducts({
      keyword: searchKeyword.value,
      categoryId: selectedCategory.value[0] ? parseInt(selectedCategory.value[0].replace(/^(parent-|sub-|item-|sub-item-)/, '')) : undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    if (res.data) {
      products.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('搜索商品失败:', error)
    message.error('搜索商品失败')
  }
}

// 处理分类悬停
const handleCategoryHover = (category: CategoryVO) => {
  hoveredCategory.value = category
  showSubMenu.value = true
}

// 处理菜单鼠标进入
const handleMenuMouseEnter = () => {
  showSubMenu.value = true
}

// 选择分类
const handleCategorySelect = (categoryId: string) => {
  selectedCategory.value = [categoryId]
  currentPage.value = 1
  showSubMenu.value = false
  handleSearch()
  // 阻止事件冒泡
  event?.stopPropagation()
}

// 翻页
const handlePageChange = (page: number) => {
  currentPage.value = page
  handleSearch()
}

// 添加到购物车
const addToCart = (product: ProductVO) => {
  message.success('添加到购物车成功')
}

// 查看商品详情
const viewProduct = (product: ProductVO) => {
  router.push(`/product/${product.id}`)
}

// 在script部分添加handleSubCategoryClick函数
// 选择分类
const handleSubCategoryClick = (category: CategoryVO) => {
  selectedCategory.value = [`item-${category.id}`]
  currentPage.value = 1
  showSubMenu.value = false
  handleSearch()
  // 阻止事件冒泡
  event?.stopPropagation()
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
}

.sub-categories {
  position: absolute;
  left: 200px;
  top: 0;
  min-width: 200px;
  max-width: 300px;
  min-height: 100%;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  padding: 16px;
  z-index: 99;
}

.sub-category-section {
  margin-bottom: 16px;
}

.sub-category-section h4 {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub-category-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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
}

.sub-category-items a:hover {
  color: #1890ff;
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

.submenu-arrow {
  margin-left: auto;
}

.sub-category-link {
  display: inline-block;
  color: #666;
  font-size: 12px;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.sub-category-link:hover {
  color: #1890ff;
  background: #e6f7ff;
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