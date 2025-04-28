<template>
  <div class="order-list">
    <a-list
      :data-source="orders"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card :title="'Order number:' + item.orderNo" style="width: 100%">
            <template #extra>
              <a-tag :color="getStatusColor(item.status)">
                {{ getStatusText(item.status) }}
              </a-tag>
            </template>
            
            <!-- 商品列表 -->
            <div class="order-items">
              <div v-for="product in item.products" :key="product.id" class="order-item">
                <img :src="getProductImageUrl(product.image)" :alt="product.name" class="product-image" />
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-price">¥{{ product.price.toFixed(2) }} × {{ product.quantity }}</div>
                </div>
              </div>
            </div>
            
            <!-- 订单信息 -->
            <div class="order-info">
              <div class="order-total">
                total:<span class="price">¥{{ item.totalAmount.toFixed(2) }}</span>
              </div>
              <div class="order-actions">
                <a-button
                  v-if="parseInt(item.status) === 0"
                  type="primary"
                  @click="handlePay(item)"
                >
                  Pay immediately
                </a-button>
                <a-button
                  v-if="parseInt(item.status) === 0"
                  @click="handleCancel(item)"
                >
                  Cancel the order
                </a-button>
                <a-button
                  v-if="parseInt(item.status) === 1"
                  @click="handleCancel(item)"
                >
                  Cancel the order
                </a-button>
                <a-button
                  v-if="parseInt(item.status) === 2"
                  type="primary"
                  @click="handleConfirm(item)"
                >
                  Confirm receipt
                </a-button>
                <a-button
                  v-if="parseInt(item.status) === 3"
                  type="primary"
                  @click="handleReview(item)"
                >
                  Review
                </a-button>
                <a-button
                  v-if="parseInt(item.status) === 3"
                  type="danger"
                  @click="handleRefund(item)"
                >
                  Apply for refund
                </a-button>
              </div>
            </div>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getOrders, payOrder, cancelOrder, confirmOrder, refundOrder } from '@/api/order'
import { getImageUrl } from '@/utils/imageUtil'
import { useRouter } from 'vue-router'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { h } from 'vue'

const props = defineProps<{
  status: string | null
}>()

// 添加调试日志，监控status属性
// console.log('OrderList初始化，接收到的status参数:', props.status, typeof props.status);

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
  status: string
  totalAmount: number
  products: OrderProduct[]
}

const orders = ref<Order[]>([])
const loading = ref(false)

// 添加加载标志，确保组件能正确加载数据
const isLoaded = ref(false)

const router = useRouter()

const loadOrders = async () => {
  try {
    loading.value = true;
    
    // 构建请求参数
    const params: any = { 
      page: 1, 
      size: 10 
    };
    
    // 只有在status不为null且不为undefined和空字符串时才添加到请求参数
    if (props.status !== null && props.status !== undefined && props.status !== '') {
      params.status = props.status;
      console.log(`正在按状态 [${props.status}] (类型: ${typeof props.status}) 加载订单`);
    } else {
      console.log('加载所有状态的订单');
    }
    
    // 发送请求
    const res = await getOrders(params);
    console.log('获取订单API响应:', res);
    
    // 处理 ApiResponse 格式的响应
    if (res.code === 200 && res.data) {
      const responseData = res.data;
      
      // 处理 PageResult 格式的数据
      if (responseData.records && Array.isArray(responseData.records)) {
        orders.value = responseData.records;
        console.log('从 records 字段加载订单数据，数量:', orders.value.length);
      } else if (responseData.list && Array.isArray(responseData.list)) {
        orders.value = responseData.list;
        console.log('从 list 字段加载订单数据，数量:', orders.value.length);
      } else if (Array.isArray(responseData)) {
        orders.value = responseData;
        console.log('直接从 data 字段加载订单数组，数量:', orders.value.length);
      } else {
        console.error('订单数据格式异常:', responseData);
        orders.value = [];
      }
      
      // 打印每个订单信息帮助调试
      orders.value.forEach((order, index) => {
        console.log(`订单[${index}]: ID=${order.id}, 订单号=${order.orderNo}, 状态=${order.status}`);
      });
    } else {
      console.error('API 返回错误:', res);
      message.error(res.msg || '加载订单失败');
      orders.value = [];
    }
    
    isLoaded.value = true;
  } catch (error) {
    console.error('加载订单失败:', error);
    message.error('加载订单失败');
    orders.value = [];
  } finally {
    loading.value = false;
  }
}

const getStatusColor = (status: string) => {
  // 将字符串转为数字
  const statusNum = parseInt(status)
  
  // 根据数字状态码返回颜色
  switch(statusNum) {
    case 0: return 'orange' // 待付款
    case 1: return 'blue'   // 待发货
    case 2: return 'cyan'   // 待收货
    case 3: return 'green'  // 已完成
    case 4: return 'red'    // 已取消
    case 5: return 'purple' // 已退款，添加
    default: return 'default'
  }
}

const getStatusText = (status: string) => {
  // 将字符串转为数字
  const statusNum = parseInt(status)
  
  // 根据数字状态码返回文本
  switch(statusNum) {
    case 0: return 'Pending payment'
    case 1: return 'To be shipped'
    case 2: return 'To be received'
    case 3: return 'Done'
    case 4: return 'Canceled'
    case 5: return 'Refunded'  // 添加退款状态
    default: return status
  }
}

const handlePay = async (order: Order) => {
  try {
    const response = await payOrder(order.orderNo);
    console.log('支付响应:', response);
    
    if (response && response.code === 200) {
      message.success('The payment was successful');
      loadOrders();
    } else {
      message.error((response && response.msg) || 'The payment failed');
    }
  } catch (error) {
    console.error('支付失败:', error);
    message.error('The payment failed');
  }
}

const handleCancel = async (order: Order) => {
  try {
    const response = await cancelOrder(order.orderNo);
    console.log('取消订单响应:', response);
    
    if (response && response.code === 200) {
      message.success('The cancellation of the order is successful');
      loadOrders();
    } else {
      message.error((response && response.msg) || 'Failed to cancel the order');
    }
  } catch (error) {
    console.error('取消订单失败:', error);
    message.error('Failed to cancel the order');
  }
}

const handleConfirm = async (order: Order) => {
  try {
    const response = await confirmOrder(order.orderNo);
    console.log('确认收货响应:', response);
    
    if (response && response.code === 200) {
      message.success('Confirm that the receipt is successful');
      loadOrders();
    } else {
      message.error((response && response.msg) || 'Failed to confirm receipt');
    }
  } catch (error) {
    console.error('确认收货失败:', error);
    message.error('Failed to confirm receipt');
  }
}

// 处理评价按钮点击事件
const handleReview = (order: Order) => {
  console.log('准备评价订单:', order.orderNo);
  // 跳转到评价页面，传递订单信息
  router.push({
    path: '/review',
    query: {
      orderNo: order.orderNo,
      productId: order.products[0].id.toString()  // 如果只有一个商品，直接使用第一个
    }
  });
}

// 添加退款处理函数
const handleRefund = (order: Order) => {
  Modal.confirm({
    title: '确认申请退款',
    icon: () => h(ExclamationCircleOutlined),
    content: '确定要申请退款吗？此操作无法撤销。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await refundOrder(order.orderNo);
        message.success('退款申请成功');
        // 刷新订单列表
        loadOrders();
      } catch (error) {
        console.error('申请退款失败:', error);
        message.error('申请退款失败');
      }
    }
  });
};

// 监听status变化，重新加载订单
watch(() => props.status, (newStatus) => {
  console.log('The order status parameter changes:', newStatus);
  loadOrders();
}, { immediate: true }); // 添加immediate: true确保组件初始化时就加载数据

// 初始化时也加载一次，确保status为null时也能加载数据
onMounted(() => {
  console.log('OrderList组件已挂载，status=', props.status, '类型:', typeof props.status);
  // 增加调试信息，检查空字符串状态
  if (props.status === '') {
    console.log('检测到空字符串状态，这应该触发加载全部订单');
  }
  if (!isLoaded.value) {
    loadOrders();
  }
});

const getProductImageUrl = (url: string) => {
  return getImageUrl(url);
}
</script>

<style scoped>
.order-list {
  margin-top: 16px;
}

.order-items {
  margin: 16px 0;
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
}

.product-info {
  flex: 1;
}

.product-name {
  margin-bottom: 8px;
}

.product-price {
  color: #999;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  font-size: 16px;
}

.price {
  color: #f5222d;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style> 