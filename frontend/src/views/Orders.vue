<template>
  <div class="my-orders-page">
    <h1 class="page-title">My Orders</h1>

    <a-tabs v-model:activeKey="activeStatus" @change="handleStatusChange">
      <a-tab-pane key="" tab="All orders"></a-tab-pane>
      <a-tab-pane key="0" tab="Pending payment"></a-tab-pane>
      <a-tab-pane key="1" tab="To be shipped"></a-tab-pane>
      <a-tab-pane key="2" tab="To be received"></a-tab-pane>
      <a-tab-pane key="3" tab="Completed"></a-tab-pane>
      <a-tab-pane key="4" tab="Cancelled"></a-tab-pane>
      <a-tab-pane key="5" tab="Refunded"></a-tab-pane>
    </a-tabs>

    <div v-if="loading" class="loading-container">
      <a-spin tip="Loading..."></a-spin>
    </div>

    <div v-else-if="orders.length === 0" class="empty-container">
      <a-empty description="No orders yet"></a-empty>
    </div>

    <div v-else class="order-list">
      <div v-for="item in orders" :key="item.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">Order number: {{ item.orderNo }}</span>
            <span class="order-date">{{ formatDate(item.createTime) }}</span>
          </div>
          <div class="order-status" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
            <!-- 调试信息 -->
            <span style="display: none;">原始状态值: {{ item.status }}</span>
          </div>
        </div>

        <div class="order-items">
          <div v-for="product in item.products" :key="product.id" class="order-item">
            <img :src="getImageUrl(product.image)" :alt="product.name" class="product-image">
            <div class="product-info">
              <div class="product-name">{{ product.name }}</div>
              <div class="product-price">¥{{ product.price }} × {{ product.quantity }}</div>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-amount">
            <span>Total {{ getTotalQuantity(item) }} items, amount: </span>
            <span class="total-price">¥{{ item.totalAmount }}</span>
          </div>
          
          <div class="order-actions">
            <!-- Actions for pending payment orders -->
            <template v-if="item.status === '0'">
              <a-button type="primary" @click="handlePayOrder(item)">Pay now</a-button>
              <a-button @click="handleCancelOrder(item)">Cancel order</a-button>
            </template>
            
            <!-- Actions for to be shipped orders -->
            <template v-if="item.status === '1'">
              <a-button @click="handleCancelOrder(item)">Cancel order</a-button>
            </template>
            
            <!-- Actions for shipped orders awaiting receipt -->
            <template v-if="item.status === '2'">
              <a-button type="primary" @click="handleConfirmOrder(item)">Confirm receipt</a-button>
            </template>
            
            <!-- Actions for completed orders -->
            <template v-if="isCompletedOrder(item)">
              <a-button type="primary" size="small" @click="handleBuyAgain(item.products)">
                Buy again
              </a-button>
              <a-button type="default" size="small" @click="showReviewDialog(item)">
                Review
              </a-button>
              <a-button type="danger" size="small" @click="handleRefundOrder(item)">
                Apply for refund
              </a-button>
              <!-- 调试信息 -->
              <span style="display: none;">状态: {{ item.status }}</span>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-container" v-if="orders.length > 0">
      <a-pagination
        v-model:current="currentPage"
        :total="total"
        :pageSize="pageSize"
        show-quick-jumper
        @change="handlePageChange"
      />
    </div>
    
    <!-- Review Dialog -->
    <a-modal
      v-model:visible="reviewDialogVisible"
      title="Product Review"
      @ok="submitReview"
      @cancel="cancelReview"
      :confirmLoading="reviewSubmitting"
      width="700px"
    >
      <template v-if="selectedOrder && selectedProduct">
        <div class="review-product">
          <img :src="getImageUrl(selectedProduct.image)" :alt="selectedProduct.name" class="product-image" />
          <div class="product-info">
            <h3 class="product-name">{{ selectedProduct.name }}</h3>
            <div class="product-price">¥{{ selectedProduct.price }}</div>
          </div>
        </div>
        
        <div class="review-form">
          <div class="rating-section">
            <p class="section-title">Rating:</p>
            <a-rate v-model:value="reviewForm.rating" allow-half />
            <span class="rating-text">{{ ratingText }}</span>
          </div>
          
          <div class="content-section">
            <p class="section-title">Review:</p>
            <a-textarea 
              v-model:value="reviewForm.content"
              placeholder="Share your thoughts about this product..." 
              :rows="4"
              :maxLength="500"
              show-count
            />
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { getOrders, payOrder, cancelOrder, confirmOrder, refundOrder } from '@/api/order';
import { submitReview as submitProductReview, hasReviewed } from '@/api/review';
import type { ReviewDTO } from '@/types/review';
import { getImageUrl } from '@/utils/imageUtil';
import type { OrderVO, OrderProductVO } from '@/types/order';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { h } from 'vue';

const router = useRouter();

// Order status constants
const ORDER_STATUS = {
  PENDING_PAYMENT: 0,  // 待付款
  PENDING_SHIPMENT: 1, // 待发货
  PENDING_RECEIPT: 2,  // 待收货
  COMPLETED: 3,        // 已完成
  CANCELLED: 4,        // 已取消
  REFUNDED: 5          // 已退款
};

// Order list data
const orders = ref<OrderVO[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const activeStatus = ref('');

// Review related
const reviewDialogVisible = ref(false);
const selectedOrder = ref<OrderVO | null>(null);
const selectedProduct = ref<OrderProductVO | null>(null);
const reviewSubmitting = ref(false);

const reviewForm = reactive({
  rating: 5,
  content: ''
});

const ratingText = computed(() => {
  const rating = reviewForm.rating;
  if (rating >= 5) return 'Excellent';
  if (rating >= 4) return 'Very Good';
  if (rating >= 3) return 'Good';
  if (rating >= 2) return 'Fair';
  return 'Poor';
});

// Load order list
const loadOrders = async () => {
  loading.value = true;
  try {
    const result = await getOrders({
      status: activeStatus.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    });
    
    console.log('订单数据:', result);
    
    if (result.code === 200 && result.data) {
      orders.value = result.data.records?.map((order: any) => {
        console.log('单个订单数据:', order);
        return {
          id: order.id,
          orderNo: order.orderNo,
          status: order.status,
          totalAmount: order.totalAmount,
          products: order.products,
          createTime: order.createdTime || '',
          updateTime: order.updatedTime || ''
        };
      }) || [];
      
      console.log('处理后的订单数据:', orders.value);
      total.value = result.data.total || 0;
    } else {
      message.error(result.msg || 'Failed to load orders');
      orders.value = [];
      total.value = 0;
    }
  } catch (error: any) {
    console.error('Failed to load orders:', error);
    message.error(error.message || 'Failed to load orders');
    orders.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// Handle status tab change
const handleStatusChange = (status: string) => {
  activeStatus.value = status;
  currentPage.value = 1;
  loadOrders();
};

// Handle pagination change
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadOrders();
};

// Get status text
const getStatusText = (status: string | number) => {
  const statusNum = parseInt(status.toString());
  switch (statusNum) {
    case ORDER_STATUS.PENDING_PAYMENT:
      return '待付款';
    case ORDER_STATUS.PENDING_SHIPMENT:
      return '待发货';
    case ORDER_STATUS.PENDING_RECEIPT:
      return '待收货';
    case ORDER_STATUS.COMPLETED:
      return '已完成';
    case ORDER_STATUS.CANCELLED:
      return '已取消';
    case ORDER_STATUS.REFUNDED:
      return '已退款';
    default:
      return '未知状态';
  }
};

// Get status CSS class
const getStatusClass = (status: string) => {
  console.log('获取状态样式类，状态值:', status);
  switch (status) {
    case '0': return 'status-pending';
    case '1': return 'status-shipping';
    case '2': return 'status-shipped';
    case '3': return 'status-completed';
    case '4': return 'status-cancelled';
    case '5': return 'status-refunded';
    default: return '';
  }
};

// Format date
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

// Calculate total quantity in order
const getTotalQuantity = (order: OrderVO) => {
  return order.products.reduce((total, product) => total + product.quantity, 0);
};

// Handle pay order
const handlePayOrder = async (order: OrderVO) => {
  try {
    await payOrder(order.orderNo);
    message.success('Payment successful');
    loadOrders();
  } catch (error: any) {
    console.error('Payment failed:', error);
    message.error(error.message || 'Payment failed');
  }
};

// Handle cancel order
const handleCancelOrder = (order: OrderVO) => {
  Modal.confirm({
    title: 'Cancel Order',
    content: 'Are you sure you want to cancel this order?',
    okText: 'Yes',
    cancelText: 'No',
    onOk: async () => {
      try {
        await cancelOrder(order.orderNo);
        message.success('Order cancelled successfully');
        loadOrders();
      } catch (error: any) {
        console.error('Failed to cancel order:', error);
        message.error(error.message || 'Failed to cancel order');
      }
    }
  });
};

// Handle confirm order receipt
const handleConfirmOrder = (order: OrderVO) => {
  Modal.confirm({
    title: 'Confirm Receipt',
    content: 'Are you sure you want to confirm receipt of this order?',
    okText: 'Yes',
    cancelText: 'No',
    onOk: async () => {
      try {
        await confirmOrder(order.orderNo);
        message.success('Order confirmed successfully');
        loadOrders();
      } catch (error: any) {
        console.error('Failed to confirm order:', error);
        message.error(error.message || 'Failed to confirm order');
      }
    }
  });
};

// Handle buy again
const handleBuyAgain = (products: OrderProductVO[]) => {
  // Implement buying again logic
  console.log('Buy again:', products);
  message.info('Buy again feature will be implemented soon');
};

// Show review dialog
const showReviewDialog = async (order: OrderVO) => {
  selectedOrder.value = order;
  
  // If there's only one product, select it automatically
  if (order.products.length === 1) {
    await selectProductForReview(order.products[0]);
  } else {
    // TODO: Show product selection dialog
    await selectProductForReview(order.products[0]); // For now, just select the first product
  }
};

const selectProductForReview = async (product: OrderProductVO) => {
  // Check if the user has already reviewed this product
  if (!selectedOrder.value) return;
  
  try {
    const response = await hasReviewed(selectedOrder.value.orderNo, product.id);
    
    if (response.code === 200) {
      if (response.data === true) {
        message.info('You have already reviewed this product');
        return;
      }
      
      selectedProduct.value = product;
      resetReviewForm();
      reviewDialogVisible.value = true;
    } else {
      message.error(response.msg || 'Failed to check review status');
    }
  } catch (error) {
    console.error('Failed to check review status:', error);
    message.error('Failed to check if you have already reviewed this product');
  }
};

const resetReviewForm = () => {
  reviewForm.rating = 5;
  reviewForm.content = '';
};

const cancelReview = () => {
  reviewDialogVisible.value = false;
  selectedOrder.value = null;
  selectedProduct.value = null;
  resetReviewForm();
};

// Submit review
const submitReview = async () => {
  if (!selectedOrder.value || !selectedProduct.value) {
    message.error('No product selected for review');
    return;
  }
  
  if (!reviewForm.rating) {
    message.error('Please select a rating');
    return;
  }
  
  try {
    reviewSubmitting.value = true;
    
    const reviewData: ReviewDTO = {
      orderNo: selectedOrder.value.orderNo,
      productId: selectedProduct.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content
    };
    
    console.log('提交评价数据:', reviewData);
    
    const result = await submitProductReview(reviewData);
    console.log('评价提交结果:', result);
    
    if (result.code === 200) {
      message.success('Review submitted successfully');
      reviewDialogVisible.value = false;
      selectedOrder.value = null;
      selectedProduct.value = null;
      resetReviewForm();
    } else {
      throw new Error(result.msg || 'Failed to submit review');
    }
  } catch (error: any) {
    console.error('Failed to submit review:', error);
    message.error(error.message || 'Failed to submit review');
  } finally {
    reviewSubmitting.value = false;
  }
};

// 添加一个辅助函数，判断是否是已完成订单
const isCompletedOrder = (order: OrderVO) => {
  console.log('检查是否是已完成订单:', order.orderNo, order.status);
  // 只有当状态值为3（已完成）时才返回true
  return order.status === '3';
};

// 处理退款申请
const handleRefundOrder = (order: OrderVO) => {
  Modal.confirm({
    title: '确认申请退款',
    icon: () => h(ExclamationCircleOutlined),
    content: '确定要申请退款吗？申请后无法撤销。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await refundOrder(order.orderNo);
        message.success('退款申请成功');
        loadOrders();
      } catch (error) {
        console.error('退款申请失败:', error);
        message.error('退款申请失败，请稍后再试');
      }
    },
  });
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.my-orders-page {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.order-list {
  margin-top: 20px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.order-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-no {
  font-weight: bold;
  margin-right: 16px;
}

.order-date {
  color: #8c8c8c;
}

.order-status {
  font-weight: bold;
}

.status-pending {
  color: #fa8c16;
}

.status-shipping,
.status-shipped {
  color: #1890ff;
}

.status-completed {
  color: #52c41a;
}

.status-cancelled {
  color: #bfbfbf;
}

.status-refunded {
  color: #722ed1; /* 紫色，使退款状态更加明显 */
}

.order-items {
  padding: 16px;
}

.order-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 500;
  margin-bottom: 8px;
}

.product-price {
  color: #8c8c8c;
}

.order-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-price {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 18px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* Review dialog styles */
.review-product {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.review-product .product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
  border-radius: 4px;
}

.review-product .product-info {
  flex: 1;
}

.review-product .product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.review-product .product-price {
  color: #f5222d;
  font-weight: bold;
}

.review-form {
  margin-top: 16px;
}

.review-form .section-title {
  margin-bottom: 8px;
  font-weight: 500;
}

.content-section {
  margin-bottom: 16px;
}

.rating-section {
  margin-bottom: 16px;
}

.rating-text {
  margin-left: 8px;
}
</style> 