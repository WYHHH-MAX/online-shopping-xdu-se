<template>
  <div class="product-detail">
    <!-- 添加面包屑导航 -->
    <div class="breadcrumb">
      <a-breadcrumb>
        <a-breadcrumb-item>
          <router-link to="/">首页</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>
          <router-link :to="`/category/${product.categoryId}`">{{ getCategoryName(product.categoryId) }}</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>{{ product.name }}</a-breadcrumb-item>
      </a-breadcrumb>
    </div>
    
    <a-row :gutter="24" v-if="!loading">
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
                :src="getImageUrl(product.mainImage)" 
                :alt="product.name" 
                class="main-image" 
                ref="mainImage"
                @load="handleImageLoaded"
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
              v-for="(image, index) in getImageUrls(product.images || [])"
              :key="index"
              class="thumbnail-wrapper"
              @click="handleImageClick(index)"
            >
              <img 
                :src="image" 
                :alt="`${product.name} - 图片 ${index + 1}`"
                class="thumbnail" 
                :class="{ active: currentImageIndex === index }" 
              />
            </div>
          </div>
        </div>
      </a-col>
      <a-col :span="12">
        <div class="product-info">
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="meta-info">
            <span class="category" v-if="product.categoryId">分类: {{ getCategoryName(product.categoryId) }}</span>
            <span class="seller">商家: {{ product.sellerId }}</span>
          </div>
          <div class="price">¥{{ product.price }}</div>
          <div class="stats">
            <div class="sales">销量: {{ product.sales }}</div>
            <div class="stock" :class="{ 'low-stock': product.stock < 10 }">
              库存: {{ product.stock }}
              <span v-if="product.stock < 10" class="stock-warning">库存紧张</span>
            </div>
          </div>
          <div class="divider"></div>
          <div class="description">
            <h3>商品描述</h3>
            <p>{{ product.description }}</p>
          </div>
          <div class="actions">
            <div class="quantity-control">
              <span class="label">数量:</span>
              <a-input-number
                v-model:value="quantity"
                :min="1"
                :max="product.stock"
                size="large"
              />
            </div>
            <a-button type="primary" size="large" @click="handleAddToCart" :disabled="!product.stock">
              <shopping-cart-outlined />
              加入购物车
            </a-button>
            <a-button type="danger" size="large" @click="handleBuyNow" :disabled="!product.stock">
              <dollar-outlined />
              立即购买
            </a-button>
          </div>
          <div class="error-message" v-if="error">{{ error }}</div>
        </div>
      </a-col>
    </a-row>
    <div class="loading-container" v-else>
      <a-spin size="large" tip="加载商品信息中..." />
    </div>
    
    <!-- 推荐商品 -->
    <div class="recommended-products" v-if="!loading && recommendedProducts.length > 0">
      <h2 class="section-title">推荐商品</h2>
      <div class="product-grid">
        <div 
          v-for="item in recommendedProducts" 
          :key="item.id" 
          class="product-card"
          @click="navigateToProduct(item.id)"
        >
          <img :src="getImageUrl(item.mainImage)" :alt="item.name" class="product-image" />
          <div class="product-card-content">
            <h3 class="product-name">{{ item.name }}</h3>
            <p class="product-price">¥{{ item.price }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProductDetail, getFeaturedProducts } from '@/api/product';
import { message } from 'ant-design-vue';
import { getImageUrl, getImageUrls } from '@/utils/imageUtil';
import type { ProductVO } from '@/types/product';
import { ShoppingCartOutlined, DollarOutlined } from '@ant-design/icons-vue';

const route = useRoute();
const router = useRouter();
const product = ref<ProductVO>({} as ProductVO);
const quantity = ref(1);
const loading = ref(true);
const error = ref('');
const previewVisible = ref(false);
const currentImageIndex = ref(0);
const recommendedProducts = ref<ProductVO[]>([]);

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
  if (!mainImage.value) return {};
  
  const imgWidth = mainImage.value.naturalWidth || mainImage.value.width;
  const imgHeight = mainImage.value.naturalHeight || mainImage.value.height;
  
  // 计算实际缩放比例
  const scaleX = imgWidth / mainImage.value.width;
  const scaleY = imgHeight / mainImage.value.height;
  
  // 计算放大后图片的背景位置
  const x = magnifierPosition.x * scaleX * zoomFactor;
  const y = magnifierPosition.y * scaleY * zoomFactor;
  
  return {
    backgroundImage: `url(${getImageUrl(product.value.mainImage)})`,
    backgroundPosition: `-${x}px -${y}px`,
    backgroundSize: `${imgWidth * zoomFactor}px ${imgHeight * zoomFactor}px`,
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

// 图片加载完成处理
const handleImageLoaded = () => {
  logImageDetails();
};

const loadProductDetail = async () => {
  loading.value = true;
  error.value = '';
  try {
    const productId = parseInt(route.params.id as string);
    console.log('加载商品详情, ID:', productId);
    const res = await getProductDetail(productId);
    console.log('获取到商品详情原始响应:', res);
    
    if (res && res.data) {
      product.value = res.data;
      console.log('处理后的商品详情:', {
        id: product.value.id,
        name: product.value.name,
        price: product.value.price,
        mainImage: product.value.mainImage,
        处理后的主图URL: getImageUrl(product.value.mainImage),
        图片数量: product.value.images?.length || 0
      });
      
      // 加载推荐商品，但排除当前商品
      loadRecommendedProducts(productId);
      
      // 在DOM更新后检查图片加载情况
      setTimeout(logImageDetails, 1000);
    } else {
      error.value = '商品信息获取失败';
      console.error('商品信息获取失败，返回数据:', res);
    }
  } catch (err: any) {
    console.error('获取商品详情失败:', err);
    error.value = err.message || '获取商品详情失败';
    message.error('获取商品详情失败');
  } finally {
    loading.value = false;
  }
};

const loadRecommendedProducts = async (currentProductId: number) => {
  try {
    const res = await getFeaturedProducts();
    if (res && res.data) {
      // 从响应中提取推荐商品列表
      let products: ProductVO[] = [];
      const responseData = res.data as any;
      
      if (responseData.list) {
        products = responseData.list;
      } else if (responseData.code === 200 && responseData.data && responseData.data.list) {
        products = responseData.data.list;
      } else if (Array.isArray(responseData)) {
        products = responseData;
      }
      
      // 过滤掉当前商品
      recommendedProducts.value = products.filter(p => p.id !== currentProductId).slice(0, 4);
    }
  } catch (error) {
    console.error('获取推荐商品失败:', error);
  }
};

const getCategoryName = (categoryId: number): string => {
  // 这里可以根据分类ID获取分类名称
  // 如果有分类缓存或者全局状态，可以从中获取
  return `分类${categoryId}`;
};

const handleImageClick = (index: number) => {
  currentImageIndex.value = index;
  // 选择显示的主图
  if (product.value.images && product.value.images.length > index) {
    product.value.mainImage = product.value.images[index];
  }
};

const handleAddToCart = () => {
  // 处理加入购物车逻辑
  message.success(`已添加 ${quantity.value} 件商品到购物车`);
};

const handleBuyNow = () => {
  // 处理立即购买逻辑
  router.push({
    path: '/checkout',
    query: {
      products: JSON.stringify([{
        id: product.value.id,
        quantity: quantity.value
      }])
    }
  });
};

const navigateToProduct = (productId: number) => {
  router.push(`/product/${productId}`);
};

onMounted(() => {
  loadProductDetail();
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

.thumbnail-wrapper {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail-wrapper:hover, .thumbnail-wrapper:has(.active) {
  border-color: #1890ff;
  transform: translateY(-2px);
}

.thumbnail {
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
}

.section-title {
  font-size: 20px;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 4px solid #1890ff;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
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
  height: 180px;
  object-fit: cover;
}

.product-card-content {
  padding: 12px;
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
</style> 