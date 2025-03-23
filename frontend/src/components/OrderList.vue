<template>
  <div class="order-list">
    <a-list
      :data-source="orders"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card :title="'订单号：' + item.orderNo" style="width: 100%">
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
                总计：<span class="price">¥{{ item.totalAmount.toFixed(2) }}</span>
              </div>
              <div class="order-actions">
                <a-button
                  v-if="parseInt(item.status) === 0"
                  type="primary"
                  @click="handlePay(item)"
                >
                  立即支付
                </a-button>
                <a-button
                  v-if="parseInt(item.status) === 0"
                  @click="handleCancel(item)"
                >
                  取消订单
                </a-button>
                <a-button
                  v-if="parseInt(item.status) === 1"
                  type="primary"
                  @click="handleConfirm(item)"
                >
                  确认收货
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
import { message } from 'ant-design-vue'
import { getOrders, payOrder, cancelOrder, confirmOrder } from '@/api/order'

const props = defineProps<{
  status: string | null
}>()

// 添加调试日志，监控status属性
console.log('OrderList初始化，接收到的status参数:', props.status, typeof props.status);

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
    
    console.log('正在加载订单，请求URL: /orders，参数:', params, '参数类型:', 
      Object.entries(params).map(([k, v]) => `${k}: ${typeof v}`).join(', '));
    
    // 发送请求
    const res = await getOrders(params);
    console.log('获取订单API响应:', res);
    
    // 处理响应数据
    if (res && res.list) {
      // 使用list字段
      orders.value = res.list;
      console.log('成功获取订单列表，数量:', orders.value.length);
      
      // DEBUG: 打印每个订单信息
      orders.value.forEach((order, index) => {
        console.log(`订单[${index}]: ID=${order.id}, 订单号=${order.orderNo}, 状态=${order.status}`);
      });
    } else if (res && res.records) {
      // 使用records字段
      orders.value = res.records;
      console.log('成功获取订单列表（从records字段），数量:', orders.value.length);
      
      orders.value.forEach((order, index) => {
        console.log(`订单[${index}]: ID=${order.id}, 订单号=${order.orderNo}, 状态=${order.status}`);
      });
    } else if (res && (res as any).data) {
      // 数据在data字段中
      const data = (res as any).data;
      if (Array.isArray(data)) {
        // data直接是数组
        orders.value = data;
      } else if (data.list && Array.isArray(data.list)) {
        // data.list是数组
        orders.value = data.list;
      } else if (data.records && Array.isArray(data.records)) {
        // data.records是数组
        orders.value = data.records;
      } else {
        console.error('订单数据格式异常，data字段结构不符合预期:', data);
        orders.value = [];
      }
      
      if (orders.value.length > 0) {
        console.log('成功获取订单列表（从data字段），数量:', orders.value.length);
        orders.value.forEach((order, index) => {
          console.log(`订单[${index}]: ID=${order.id}, 订单号=${order.orderNo}, 状态=${order.status}`);
        });
      }
    } else {
      console.error('订单数据格式异常，没有list、records或data字段:', res);
      // 尝试直接使用响应，可能返回的就是数组
      if (Array.isArray(res)) {
        console.log('响应是数组格式，直接使用');
        orders.value = res;
        orders.value.forEach((order, index) => {
          console.log(`订单[${index}]: ID=${order.id}, 订单号=${order.orderNo}, 状态=${order.status}`);
        });
      } else {
        orders.value = [];
      }
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
    default: return 'default'
  }
}

const getStatusText = (status: string) => {
  // 将字符串转为数字
  const statusNum = parseInt(status)
  
  // 根据数字状态码返回文本
  switch(statusNum) {
    case 0: return '待付款'
    case 1: return '待发货'
    case 2: return '待收货'
    case 3: return '已完成'
    case 4: return '已取消'
    default: return status
  }
}

const handlePay = async (order: Order) => {
  try {
    await payOrder(order.orderNo)
    message.success('支付成功')
    loadOrders()
  } catch (error) {
    message.error('支付失败')
  }
}

const handleCancel = async (order: Order) => {
  try {
    await cancelOrder(order.orderNo)
    message.success('取消订单成功')
    loadOrders()
  } catch (error) {
    message.error('取消订单失败')
  }
}

const handleConfirm = async (order: Order) => {
  try {
    await confirmOrder(order.orderNo)
    message.success('确认收货成功')
    loadOrders()
  } catch (error) {
    message.error('确认收货失败')
  }
}

// 监听status变化，重新加载订单
watch(() => props.status, (newStatus) => {
  console.log('订单状态参数变化:', newStatus);
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

const getProductImageUrl = (imagePath: string) => {
  // 如果已经是完整URL，直接返回
  if (imagePath.startsWith('http')) {
    return imagePath;
  }
  
  // 如果是相对路径，添加基础URL前缀
  if (imagePath.startsWith('/')) {
    return `${import.meta.env.VITE_API_BASE_URL || '/api'}${imagePath}`;
  }
  
  // 如果没有/前缀，添加完整路径
  return `${import.meta.env.VITE_API_BASE_URL || '/api'}/images/products/${imagePath}`;
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