<template>
  <div class="review-page">
    <a-page-header
      title="Write a Review"
      @back="() => $router.go(-1)"
    />
    
    <div class="review-content" v-if="product">
      <div class="product-info">
        <img :src="getImageUrl(product.image)" :alt="product.name" class="product-image" />
        <div class="product-details">
          <h2 class="product-name">{{ product.name }}</h2>
          <div class="product-price">¥{{ product.price }}</div>
        </div>
      </div>
      
      <a-divider />
      
      <div class="review-form">
        <div class="form-item">
          <div class="form-label">Rating</div>
          <div class="form-input">
            <a-rate v-model:value="reviewForm.rating" :tooltips="ratingLabels" allow-half />
          </div>
        </div>
        
        <div class="form-item">
          <div class="form-label">Review</div>
          <div class="form-input">
            <a-textarea 
              v-model:value="reviewForm.content"
              placeholder="Share your experience with this product..."
              :rows="6"
              :maxLength="500"
              show-count
            />
          </div>
        </div>
        
        <div class="submit-container">
          <a-button type="primary" :loading="submitting" @click="submitReview">Submit Review</a-button>
        </div>
      </div>
    </div>
    
    <div v-else-if="loading" class="loading-container">
      <a-spin size="large" />
    </div>
    
    <div v-else class="error-container">
      <a-result
        status="error"
        title="Failed to load product information"
        :sub-title="error || 'Please try again later'"
      >
        <template #extra>
          <a-button type="primary" @click="fetchProductInfo">Retry</a-button>
          <a-button @click="() => $router.go(-1)">Go Back</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { getImageUrl } from '@/utils/imageUtil';
import { submitReview as submitProductReview, hasReviewed } from '@/api/review';
import { getOrderDetail } from '@/api/order';

const route = useRoute();
const router = useRouter();

// 产品信息
const product = ref<any>(null);
const loading = ref(true);
const error = ref('');

// 评价表单
const reviewForm = reactive({
  rating: 5,
  content: ''
});

const submitting = ref(false);

// 评分文本标签
const ratingLabels = ['Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];

// 获取URL参数
const orderNo = route.query.orderNo as string;
const productId = parseInt(route.query.productId as string);

// 检查必需参数
if (!orderNo || !productId) {
  message.error('Missing required parameters');
  router.push('/orders');
}

// 获取订单商品信息
const fetchProductInfo = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    console.log('获取评价参数: orderNo =', orderNo, 'productId =', productId);
    
    // 先检查是否已经评价过
    const hasReviewedResponse = await hasReviewed(orderNo, productId);
    console.log('评价检查响应:', hasReviewedResponse);
    
    if (hasReviewedResponse.code === 200) {
      if (hasReviewedResponse.data === true) {
        message.info('You have already reviewed this product');
        router.push('/orders');
        return;
      }
    } else {
      throw new Error(hasReviewedResponse.msg || 'Failed to check review status');
    }
    
    // 获取订单详情
    const orderResponse = await getOrderDetail(orderNo);
    console.log('订单详情响应:', orderResponse);
    
    if (orderResponse.code !== 200 || !orderResponse.data) {
      throw new Error(orderResponse.msg || 'Failed to load order information');
    }
    
    const orderData = orderResponse.data;
    
    if (!orderData.products || orderData.products.length === 0) {
      throw new Error('No products found in this order');
    }
    
    // 找到指定产品
    const foundProduct = orderData.products.find((p: any) => p.id === productId);
    console.log('找到的商品:', foundProduct);
    
    if (!foundProduct) {
      throw new Error('Product not found in this order');
    }
    
    product.value = foundProduct;
  } catch (err: any) {
    console.error('Failed to fetch product information:', err);
    error.value = err.message || 'Failed to load product information';
  } finally {
    loading.value = false;
  }
};

// 提交评价
const submitReview = async () => {
  if (!product.value) {
    message.error('Product information not available');
    return;
  }
  
  if (!reviewForm.rating) {
    message.error('Please select a rating');
    return;
  }
  
  try {
    submitting.value = true;
    
    const reviewData = {
      orderNo,
      productId,
      rating: reviewForm.rating,
      content: reviewForm.content
    };
    
    console.log('提交评价数据:', reviewData);
    
    const response = await submitProductReview(reviewData);
    console.log('评价提交响应:', response);
    
    if (response.code === 200) {
      message.success('Review submitted successfully');
      setTimeout(() => {
        router.push('/orders');
      }, 1500);
    } else {
      throw new Error(response.msg || 'Failed to submit review');
    }
  } catch (err: any) {
    console.error('Failed to submit review:', err);
    message.error(err.message || 'Failed to submit review');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  fetchProductInfo();
});
</script>

<style scoped>
.review-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.review-content {
  background: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 16px;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.product-price {
  color: #ff4d4f;
  font-weight: bold;
}

.review-form {
  margin-top: 24px;
}

.form-item {
  margin-bottom: 24px;
}

.form-label {
  font-weight: 500;
  margin-bottom: 8px;
}

.submit-container {
  margin-top: 32px;
  text-align: center;
}

.loading-container,
.error-container {
  padding: 48px 0;
  text-align: center;
}
</style> 