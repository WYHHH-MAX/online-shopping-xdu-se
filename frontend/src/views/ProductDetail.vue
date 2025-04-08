<template>
  <div class="product-detail">
    <!-- 加载中状态 -->
    <a-spin :spinning="loading" tip="Loading...">
      <!-- 添加面包屑导航 -->
      <div class="breadcrumb" v-if="product">
        <a-breadcrumb>
          <a-breadcrumb-item>
            <router-link to="/">Home</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item v-if="productCategoryId">
            <router-link :to="`/category/${productCategoryId}`">{{ getCategoryName(productCategoryId) }}</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>{{ productName }}</a-breadcrumb-item>
        </a-breadcrumb>
      </div>
      
      <a-row :gutter="24" v-if="product">
        <a-col :span="12">
          <div class="product-images">
            <!-- 图片放大镜组件 -->
            <div class="magnifier-container">
              <div 
                class="product-image-container" 
                ref="imageContainer"
                @mousemove="handleMouseMove"
                @mouseenter="showMagnifier = true"
                @mouseleave="showMagnifier = false"
              >
                <img 
                  :src="getImageUrl(productMainImage)" 
                  :alt="productName" 
                  class="main-image" 
                  ref="mainImage"
                  @load="handleImageLoaded"
                  @error="handleImageError"
                />
                <div 
                  v-show="showMagnifier" 
                  class="magnifier" 
                  :style="magnifierStyle"
                ></div>
                <div 
                  v-show="showMagnifier" 
                  class="zoomed-image" 
                  :style="zoomedImageStyle"
                ></div>
              </div>
            </div>
            
            <div class="image-list">
              <div
                v-for="(image, index) in getImageUrls(productImages)"
                :key="index"
                :class="['image-item', { active: currentImageIndex === index }]"
                @click="handleImageClick(index)"
              >
                <img :src="image" :alt="productName" @error="handleImageError" />
              </div>
            </div>
          </div>
        </a-col>
        
        <!-- 商品信息部分 -->
        <a-col :span="12">
          <div class="product-info">
            <h1 class="product-title">{{ productName }}</h1>
            <div class="meta-info">
              <span class="category" v-if="productCategoryId">category: {{ getCategoryName(productCategoryId) }}</span>
              <span class="seller" v-if="productSellerId">seller: {{ sellerName }}</span>
            </div>
            <div class="price">¥{{ productPrice }}</div>
            <div class="stats">
              <div class="sales">sales: {{ productSales }}</div>
              <div class="stock" :class="{ 'low-stock': isLowStock }">
                inventory: {{ productStock }}
                <span v-if="isLowStock" class="stock-warning">Inventory is tight</span>
              </div>
            </div>
            <div class="divider"></div>
            <div class="description">
              <h3>Product description</h3>
              <p>{{ productDescription }}</p>
            </div>
            <div class="actions">
              <div class="quantity-control">
                <span class="label">quantity:</span>
                <a-input-number
                  v-model:value="quantity"
                  :min="1"
                  :max="productStock"
                  size="large"
                />
              </div>
              <a-button type="primary" size="large" @click="handleAddToCart" :disabled="isOutOfStock">
                <shopping-cart-outlined />
                Add to cart
              </a-button>
              <a-button type="danger" size="large" @click="handleBuyNow" :disabled="isOutOfStock">
                <dollar-outlined />
                Buy now
              </a-button>
            </div>
            <div class="error-message" v-if="error">{{ error }}</div>
          </div>
        </a-col>
      </a-row>
      
      <!-- 加载错误显示 -->
      <div v-if="!loading && error" class="error-container">
        <a-result status="error" :title="error">
          <template #extra>
            <a-button type="primary" @click="loadProductDetail">reload</a-button>
          </template>
        </a-result>
      </div>
    </a-spin>
    
    <!-- 商品评价部分 -->
    <div class="product-reviews" v-if="!loading && product">
      <h2 class="section-title">Product Reviews</h2>
      
      <!-- 评价摘要 -->
      <div class="review-summary" v-if="reviewSummary">
        <div class="summary-left">
          <div class="average-rating">
            <span class="rating-number">{{ reviewSummary.averageRating.toFixed(1) }}</span>
            <span class="rating-max">/5.0</span>
          </div>
          <a-rate 
            :value="reviewSummary.averageRating" 
            disabled 
            :allowHalf="true"
          />
          <div class="total-reviews">
            {{ reviewSummary.reviewCount }} reviews
          </div>
        </div>
        <div class="summary-right">
          <div class="rating-distribution">
            <div class="rating-row" v-for="n in 5" :key="n">
              <span class="stars">{{ n }} stars</span>
              <a-progress 
                :percent="calculatePercentage(getDistributionCount(n))" 
                :showInfo="false" 
                :strokeColor="'#fadb14'"
              />
              <span class="count">{{ getDistributionCount(n) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 评价列表 -->
      <div class="review-list" v-if="reviews.length > 0">
        <a-list
          itemLayout="horizontal"
          :dataSource="reviews"
          :pagination="reviewPagination"
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #avatar>
                  <a-avatar :src="item.avatar || 'https://joeschmoe.io/api/v1/random'" />
                </template>
                <template #title>
                  <div class="review-header">
                    <span class="reviewer-name">{{ item.username || 'Anonymous' }}</span>
                    <a-rate :value="item.rating" disabled />
                  </div>
                </template>
                <template #description>
                  <div class="review-content">
                    <p class="review-text">{{ item.content }}</p>
                    <div class="review-images" v-if="item.images && item.images.length > 0">
                      <a-image
                        v-for="(image, idx) in item.images"
                        :key="idx"
                        :src="image"
                        :width="100"
                        :height="100"
                        :preview="true"
                      />
                    </div>
                    <div class="review-date">{{ formatDate(item.createdTime) }}</div>
                  </div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>
      
      <!-- 无评价时显示 -->
      <a-empty 
        v-else 
        description="No reviews yet. Be the first to review this product!" 
      />
    </div>
    
    <!-- 推荐商品 -->
    <div class="recommended-products" v-if="!loading && recommendedProducts.length > 0">
      <h2 class="section-title">Recommended products</h2>
      <div class="product-grid">
        <a-card 
          v-for="item in recommendedProducts" 
          :key="item.id" 
          class="product-card"
          hoverable
          @click="navigateToProduct(item.id)"
        >
          <template #cover>
            <img :src="getImageUrl(item.mainImage || '')" :alt="item.name || 'Product image'" class="product-image" @error="handleImageError" />
          </template>
          <a-card-meta :title="item.name || 'Unknown product'" :description="`¥${item.price || 0}`" />
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProductDetail, getFeaturedProducts } from '../api/product';
import { getCategoryById } from '../api/category';
import { getSellerInfo } from '../api/seller';
import { message } from 'ant-design-vue';
import { getImageUrl, getImageUrls, handleImageError } from '../utils/imageUtil';
import type { ProductVO, PageResult } from '../types/product';
import { ShoppingCartOutlined, DollarOutlined } from '@ant-design/icons-vue';
import { addToCart, getCartCount } from '../api/cart';
import { useUserStore } from '../stores/user';
import { useCartStore } from '../stores/cart';
import { getReviewSummary, getProductReviews } from '../api/review';
import type { ProductReviewSummaryVO, ReviewVO } from '../types/review';
import dayjs from 'dayjs';

const route = useRoute();
const router = useRouter();
const product = ref<ProductVO | null>(null);
const quantity = ref(1);
const loading = ref(true);
const error = ref('');
const previewVisible = ref(false);
const currentImageIndex = ref(0);
const recommendedProducts = ref<ProductVO[]>([]);

// 商品评价相关
const reviewSummary = ref<ProductReviewSummaryVO | null>(null);
const reviews = ref<ReviewVO[]>([]);
const reviewPage = ref(1);
const reviewPageSize = ref(5);
const reviewTotal = ref(0);

// 评价分页配置
const reviewPagination = computed(() => ({
  onChange: (page: number) => {
    reviewPage.value = page;
    loadProductReviews();
  },
  pageSize: reviewPageSize.value,
  total: reviewTotal.value,
  showSizeChanger: false,
}));

// 获取评分分布计数
const getDistributionCount = (stars: number) => {
  if (!reviewSummary.value || !reviewSummary.value.ratingDistribution) return 0;
  
  const distribution = reviewSummary.value.ratingDistribution;
  switch (stars) {
    case 5: return distribution.fiveStarCount;
    case 4: return distribution.fourStarCount;
    case 3: return distribution.threeStarCount;
    case 2: return distribution.twoStarCount;
    case 1: return distribution.oneStarCount;
    default: return 0;
  }
};

// 计算百分比
const calculatePercentage = (count: number) => {
  if (!reviewSummary.value || reviewSummary.value.reviewCount === 0) return 0;
  return (count / reviewSummary.value.reviewCount) * 100;
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  return dayjs(dateString).format('YYYY-MM-DD HH:mm');
};

// 加载评价摘要
const loadReviewSummary = async () => {
  if (!product.value) return;
  
  try {
    const result = await getReviewSummary(product.value.id);
    if (result.code === 200) {
      reviewSummary.value = result.data;
    }
  } catch (error) {
    console.error('加载评价摘要失败:', error);
  }
};

// 加载商品评价
const loadProductReviews = async () => {
  if (!product.value) return;
  
  try {
    const result = await getProductReviews(product.value.id, reviewPage.value, reviewPageSize.value);
    if (result.code === 200 && result.data) {
      reviews.value = result.data.records || [];
      reviewTotal.value = result.data.total || 0;
    }
  } catch (error) {
    console.error('加载商品评价失败:', error);
  }
};

// 加载商品详情时，同时加载评价信息
watch(() => product.value?.id, (newId) => {
  if (newId) {
    loadReviewSummary();
    loadProductReviews();
  }
}, { immediate: true });

// 安全访问product属性的计算属性
const safeProduct = computed(() => {
  return product.value || {} as ProductVO;
});

// 安全的商品属性计算属性
const productName = computed(() => safeProduct.value.name || '未知商品');
const productCategoryId = computed(() => safeProduct.value.categoryId);
const productMainImage = computed(() => safeProduct.value.mainImage || '');
const productImages = computed(() => safeProduct.value.images || []);
const productPrice = computed(() => safeProduct.value.price || 0);
const productSales = computed(() => safeProduct.value.sales || 0);
const productStock = computed(() => safeProduct.value.stock || 0);
const productDescription = computed(() => safeProduct.value.description || '暂无描述');
const productSellerId = computed(() => safeProduct.value.sellerId);
const isLowStock = computed(() => productStock.value < 10);
const isOutOfStock = computed(() => productStock.value <= 0);

// 放大镜相关
const imageContainer = ref<HTMLElement | null>(null);
const mainImage = ref<HTMLImageElement | null>(null);
const showMagnifier = ref(false);
const magnifierPosition = reactive({ x: 0, y: 0 });
const zoomFactor = 2.5; // 放大倍数

// 放大镜样式
const magnifierStyle = computed(() => {
  return {
    left: `${magnifierPosition.x}px`,
    top: `${magnifierPosition.y}px`,
  };
});

// 放大后的图片样式
const zoomedImageStyle = computed(() => {
  if (!product.value || !mainImage.value) return {};
  
  const backgroundImage = `url(${getImageUrl(productMainImage.value)})`;
  const backgroundPosition = `-${magnifierPosition.x * zoomFactor}px -${magnifierPosition.y * zoomFactor}px`;
  const backgroundSize = `${mainImage.value.width * zoomFactor}px ${mainImage.value.height * zoomFactor}px`;
  
  return {
    backgroundImage,
    backgroundPosition,
    backgroundSize,
  };
});

// 处理鼠标移动事件
const handleMouseMove = (e: MouseEvent) => {
  if (!imageContainer.value || !mainImage.value) return;
  
  const rect = imageContainer.value.getBoundingClientRect();
  const imgRect = mainImage.value.getBoundingClientRect();
  
  // 计算鼠标在图片上的相对位置
  const x = e.clientX - imgRect.left;
  const y = e.clientY - imgRect.top;
  
  // 计算放大镜位置（相对于图片容器）
  magnifierPosition.x = x;
  magnifierPosition.y = y;
  
  // 边界检查
  if (magnifierPosition.x < 0) magnifierPosition.x = 0;
  if (magnifierPosition.y < 0) magnifierPosition.y = 0;
  if (magnifierPosition.x > imgRect.width) magnifierPosition.x = imgRect.width;
  if (magnifierPosition.y > imgRect.height) magnifierPosition.y = imgRect.height;
};

// 添加调试信息
const logImageDetails = () => {
  if (!mainImage.value) return;
  
  console.log('图片详情:', {
    src: mainImage.value.src,
    naturalWidth: mainImage.value.naturalWidth,
    naturalHeight: mainImage.value.naturalHeight,
    displayWidth: mainImage.value.width,
    displayHeight: mainImage.value.height,
    complete: mainImage.value.complete,
    containerRect: imageContainer.value?.getBoundingClientRect()
  });
};

// 处理图片加载
const handleImageLoaded = () => {
  if (mainImage.value) {
    // console.log('主图加载完成:', mainImage.value.src);
  }
};

// 分类和商家信息
const categoryName = ref('');
const sellerName = ref('');

const loadProductDetail = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const productId = Number(route.params.id);
    if (isNaN(productId)) {
      throw new Error('无效的商品ID');
    }
    
    const result = await getProductDetail(productId);
    product.value = result;
    
    // 加载分类信息
    if (result.categoryId) {
      loadCategoryInfo(result.categoryId);
    }
    
    // 加载商家信息
    if (result.sellerId) {
      loadSellerInfo(result.sellerId);
    }
    
    // 加载推荐商品
    await loadRecommendedProducts();
    
    // console.log('商品详情:', product.value);
  } catch (err: any) {
    // console.error('加载商品详情失败:', err);
    error.value = err.message || '加载商品详情失败';
  } finally {
    loading.value = false;
  }
};

// 加载分类信息
const loadCategoryInfo = async (categoryId: number) => {
  try {
    const result = await getCategoryById(categoryId);
    if (result) {
      categoryName.value = result.name || `分类${categoryId}`;
    }
  } catch (error) {
    // console.error('加载分类信息失败:', error);
    categoryName.value = `分类${categoryId}`;
  }
};

// 加载商家信息
const loadSellerInfo = async (sellerId: number) => {
  try {
    // 这里应该调用获取特定商家信息的API
    // 由于当前API似乎只支持获取当前商家信息，暂时使用ID直接显示
    sellerName.value = `商家${sellerId}`;
  } catch (error) {
    // console.error('加载商家信息失败:', error);
    sellerName.value = `商家${sellerId}`;
  }
};

// 获取分类名称
const getCategoryName = (categoryId: number): string => {
  if (categoryId === productCategoryId.value) {
    return categoryName.value || `分类${categoryId}`;
  }
  return `分类${categoryId}`;
};

// 加载推荐商品
const loadRecommendedProducts = async () => {
  try {
    const result = await getFeaturedProducts();
    if (result && result.list) {
      // 过滤掉当前商品
      const currentProductId = Number(route.params.id);
      console.log('当前商品ID:', currentProductId, '推荐商品:', result.list);
      recommendedProducts.value = result.list
        .filter((p: ProductVO) => p.id !== currentProductId)
        .slice(0, 4);
    }
  } catch (error) {
    console.error('加载推荐商品失败:', error);
  }
};

const handleImageClick = (index: number) => {
  if (!product.value) return;
  
  currentImageIndex.value = index;
  // 如果商品有图片，更新主图
  if (product.value.images && product.value.images.length > 0) {
    product.value = {
      ...product.value,
      mainImage: product.value.images[index]
    };
  }
};

/**
 * 添加到购物车
 */
const handleAddToCart = async () => {
  if (!product.value) {
    message.error('商品信息不存在');
    return;
  }
  
  try {
    // 检查用户是否已登录
    const userStore = useUserStore();
    if (!userStore.isLoggedIn()) {
      message.warning('请先登录');
      router.push('/login');
      return;
    }
    
    // 添加到购物车
    await addToCart(product.value.id, quantity.value);
    
    // 更新购物车数量
    const cartCount = await getCartCount();
    useCartStore().setCartCount(cartCount);
    
    message.success(`成功添加${quantity.value}件商品到购物车`);
  } catch (error: any) {
    console.error('添加到购物车失败:', error);
    message.error(error.message || '添加到购物车失败');
  }
};

/**
 * 处理立即购买
 */
const handleBuyNow = () => {
  if (!product.value) {
    message.error('商品信息不存在');
    return;
  }
  
  // 检查用户是否已登录
  const userStore = useUserStore();
  if (!userStore.isLoggedIn()) {
    message.warning('请先登录');
    router.push('/login');
    return;
  }
  
  // 处理立即购买逻辑 - 直接跳转到结算页面
  router.push({
    path: '/checkout',
    query: {
      // 传递单个商品信息作为直接购买
      directBuy: 'true',
      productId: product.value.id.toString(),
      quantity: quantity.value.toString()
    }
  });
};

const navigateToProduct = (productId: number) => {
  console.log('点击推荐商品，跳转到商品ID:', productId);
  // 强制刷新页面以确保数据重新加载
  if (productId === Number(route.params.id)) {
    // 如果点击的是当前商品，刷新页面
    window.location.href = `/product/${productId}`;
  } else {
    // 否则正常导航
    router.push(`/product/${productId}`);
  }
};

onMounted(async () => {
  await loadProductDetail();
  loadRecommendedProducts();
  
  if (product.value) {
    loadReviewSummary();
    loadProductReviews();
  }
});
</script>

<style scoped>
.product-detail {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px);
}

.product-images {
  text-align: center;
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
}

/* 放大镜相关样式 */
.magnifier-container {
  position: relative;
  margin-bottom: 16px;
}

.product-image-container {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: visible;
  cursor: crosshair;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.magnifier {
  position: absolute;
  width: 150px;
  height: 150px;
  border: 1px solid #ddd;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.3);
  pointer-events: none;
  z-index: 1;
  transform: translate(-50%, -50%);
}

.zoomed-image {
  position: absolute;
  top: 0;
  left: 100%;
  width: 400px;
  height: 400px;
  background-repeat: no-repeat;
  border: 1px solid #ddd;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  pointer-events: none;
  z-index: 2;
  margin-left: 20px;
}

/* 在小屏幕上调整放大图样式 */
@media (max-width: 1200px) {
  .zoomed-image {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 1000;
    width: 80vw;
    height: 80vw;
    max-width: 500px;
    max-height: 500px;
    background-color: white;
  }
}

.image-list {
  margin-top: 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.image-item {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.image-item:hover, .image-item:has(.active) {
  border-color: #1890ff;
  transform: translateY(-2px);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 0 24px;
}

.product-title {
  font-size: 24px;
  color: #262626;
  margin-bottom: 8px;
  line-height: 1.4;
}

.meta-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  color: #8c8c8c;
}

.price {
  color: #f5222d;
  font-size: 28px;
  font-weight: bold;
  margin: 16px 0;
}

.stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.sales, .stock {
  color: #595959;
  font-size: 16px;
}

.low-stock {
  color: #fa8c16;
}

.stock-warning {
  background: #fff7e6;
  color: #fa8c16;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 8px;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 24px 0;
}

.description {
  margin: 16px 0;
  color: #595959;
}

.description h3 {
  font-size: 16px;
  margin-bottom: 8px;
  color: #262626;
}

.actions {
  margin-top: 24px;
  display: flex;
  gap: 16px;
  align-items: center;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: #595959;
}

.error-message {
  margin-top: 16px;
  color: #f5222d;
  padding: 8px;
  background: #fff1f0;
  border-radius: 4px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.recommended-products {
  margin-top: 48px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 20px;
  margin-bottom: 24px;
  padding-left: 8px;
  border-left: 4px solid #1890ff;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  z-index: 1;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  z-index: 2;
}

.product-card-content {
  padding: 12px;
  pointer-events: none; /* 确保子元素不拦截点击事件 */
}

.product-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  pointer-events: none; /* 确保图片不拦截点击事件 */
}

.product-name {
  font-size: 14px;
  margin-bottom: 8px;
  color: #262626;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  color: #f5222d;
  font-weight: bold;
  margin: 0;
}

.breadcrumb {
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.ant-breadcrumb) {
  font-size: 14px;
}

:deep(.ant-breadcrumb a) {
  color: #1890ff;
}

:deep(.ant-breadcrumb a:hover) {
  color: #40a9ff;
}

.product-reviews {
  margin-top: 48px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.review-summary {
  display: flex;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.summary-left {
  flex: 0 0 200px;
  padding-right: 24px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.average-rating {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.rating-number {
  font-size: 36px;
  font-weight: bold;
  color: #fadb14;
}

.rating-max {
  font-size: 16px;
  color: #8c8c8c;
  margin-left: 4px;
}

.total-reviews {
  margin-top: 8px;
  color: #8c8c8c;
}

.summary-right {
  flex: 1;
  padding-left: 24px;
}

.rating-distribution {
  padding: 8px 0;
}

.rating-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.stars {
  flex: 0 0 60px;
  text-align: right;
  margin-right: 8px;
  color: #8c8c8c;
}

.count {
  flex: 0 0 40px;
  text-align: right;
  margin-left: 8px;
  color: #8c8c8c;
}

.review-list {
  margin-top: 24px;
}

.review-header {
  display: flex;
  align-items: center;
}

.reviewer-name {
  margin-right: 12px;
  font-weight: 500;
}

.review-content {
  margin-top: 8px;
}

.review-text {
  font-size: 14px;
  line-height: 1.6;
  color: #262626;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.review-date {
  font-size: 12px;
  color: #8c8c8c;
}

:deep(.ant-rate-star) {
  margin-right: 4px;
}

:deep(.ant-progress) {
  flex: 1;
  margin: 0 8px;
}

:deep(.ant-card-cover) {
  height: 180px;
  overflow: hidden;
}

:deep(.ant-card-cover img) {
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

:deep(.ant-card:hover .ant-card-cover img) {
  transform: scale(1.05);
}

:deep(.ant-card-meta-title) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #262626;
}

:deep(.ant-card-meta-description) {
  color: #f5222d;
  font-weight: bold;
}
</style> 