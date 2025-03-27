<template>
  <div class="seller-orders">
    <div class="orders">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="0" tab="全部订单"></a-tab-pane>
        <a-tab-pane key="1" tab="待发货"></a-tab-pane>
        <a-tab-pane key="2" tab="已发货"></a-tab-pane>
        <a-tab-pane key="3" tab="已完成"></a-tab-pane>
      </a-tabs>
      
      <a-table
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusType(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div>
              <a-button 
                type="link" 
                size="small" 
                @click="showOrderDetail(record)"
              >
                查看详情
              </a-button>
              <a-button 
                v-if="record.status === '1'" 
                type="primary" 
                size="small" 
                @click="handleShip(record.id)"
              >
                发货
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
      
      <a-modal
        v-model:visible="orderDetailVisible"
        title="订单详情"
        width="800px"
        :footer="null"
      >
        <a-descriptions bordered :column="3">
          <a-descriptions-item label="订单号" :span="3">{{ currentOrder ? currentOrder.orderNo : '无订单号' }}</a-descriptions-item>
          <a-descriptions-item label="下单时间" :span="3">{{ currentOrder ? formatTime(currentOrder.createTime) : '无下单时间' }}</a-descriptions-item>
          <a-descriptions-item label="订单金额" :span="1">{{ currentOrder ? `¥${currentOrder.totalAmount}` : '¥0.00' }}</a-descriptions-item>
          <a-descriptions-item label="状态" :span="2">
            <a-tag :color="currentOrder ? getStatusType(currentOrder.status) : 'default'">
              {{ currentOrder ? getStatusText(currentOrder.status) : '未知状态' }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
        
        <a-divider />
        
        <a-table
          :columns="productColumns"
          :data-source="currentOrder?.products || []"
          :pagination="false"
          row-key="id"
        />
        
        <div class="order-action" v-if="currentOrder && currentOrder.status === '1'">
          <a-button type="primary" @click="handleShip(currentOrder.id)">发货</a-button>
        </div>
      </a-modal>
      
      <a-modal
        v-model:visible="shipConfirmVisible"
        title="确认发货"
        @ok="confirmShip"
        @cancel="() => shipConfirmVisible = false"
        :okText="'确认发货'"
        :cancelText="'取消'"
      >
        <p>确认为该订单发货吗？发货后无法撤销。</p>
      </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getSellerOrders, shipOrder } from '@/api/seller';
import { getImageUrl } from '@/utils/imageUtil';
import type { OrderVO, OrderProductVO } from '@/types/order';

// 扩展OrderVO接口，添加我们需要的字段
interface ExtendedOrderVO extends OrderVO {
  _orderNo?: string; // 存储真实的订单号
}

const tabs = ref([
  { key: '0', name: '全部订单' },
  { key: '1', name: '待发货' },
  { key: '2', name: '已发货' },
  { key: '3', name: '已完成' }
]);

const activeTab = ref('0');
const loading = ref(false);
const data = ref<ExtendedOrderVO[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

const currentOrder = ref<ExtendedOrderVO | null>(null);
const orderDetailVisible = ref(false);
const shipConfirmVisible = ref(false);
const shipOrderNo = ref<string | null>(null);

const columns = [
  {
    title: '订单号',
    dataIndex: 'orderNo',
    key: 'orderNo'
  },
  {
    title: '下单时间',
    dataIndex: 'createTime',
    key: 'createTime',
    sorter: true,
    customRender: ({ text }: { text: string }) => formatTime(text)
  },
  {
    title: '金额',
    dataIndex: 'totalAmount',
    key: 'totalAmount',
    sorter: true,
    customRender: ({ text }: { text: number }) => `¥${text.toFixed(2)}`
  },
  {
    title: '状态',
    key: 'status'
  },
  {
    title: '操作',
    key: 'action',
    width: 150
  }
];

// 格式化时间的函数
const formatTime = (time: string | null | undefined) => {
  if (!time) return '未知时间';
  try {
    const date = new Date(time);
    if (isNaN(date.getTime())) return '时间格式错误';
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (error) {
    // console.error('时间格式化错误', error);
    return '时间格式错误';
  }
};

const productColumns = [
  { title: '商品', dataIndex: 'name', key: 'name' },
  { title: '单价', dataIndex: 'price', key: 'price' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
  { 
    title: '小计', 
    key: 'subtotal', 
    customRender: ({ text, record }: { text: any, record: any }) => 
      `¥${(record.price * record.quantity).toFixed(2)}` 
  }
];

const fetchOrders = async () => {
  loading.value = true;
  try {
    console.log('获取订单列表，参数:', {
      status: activeTab.value === '0' ? undefined : activeTab.value,
      page: pagination.current,
      size: pagination.pageSize
    });
    
    const response = await getSellerOrders({
      status: activeTab.value === '0' ? undefined : activeTab.value,
      page: pagination.current,
      size: pagination.pageSize
    });
    
    // console.log('获取到的订单数据:', response);
    
    if (response && response.data && response.data.list) {
      // console.log('订单原始数据:', response.data.list);
      
      // 处理每个订单的商品图片
      const processedOrders = response.data.list.map((order: any, index: number) => {
        // 检查订单数据完整性
        if (!order.orderNo) {
          console.warn(`订单[${index}]缺少orderNo字段:`, order);
        }
        if (!order.createTime) {
          console.warn(`订单[${index}]缺少createTime字段:`, order);
        }
        if (order.status === undefined || order.status === null) {
          console.warn(`订单[${index}]缺少status字段:`, order);
        }
        
        // 确保products字段为数组
        const products = Array.isArray(order.products) ? order.products : [];
        
        // 创建一个有效的数字id（使用索引+1确保大于0）
        const generatedId = index + 1;
        
        return {
          ...order,
          // 使用索引值+1作为伪ID，确保ID大于0
          id: generatedId,
          // 确保orderNo为字符串
          orderNo: String(order.orderNo || ''),
          // 确保status为字符串
          status: String(order.status || '0'),
          // 处理商品图片
          products: products.map((product: any) => ({
            ...product,
            image: product.image ? getImageUrl(product.image) : '/images/placeholder.jpg'
          }))
        };
      });
      
      // console.log('处理后的订单数据:', processedOrders);
      data.value = processedOrders;
      pagination.total = response.data.total || 0;
    } else {
      // console.warn('订单数据格式不正确或为空:', response);
      data.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    // console.error('获取订单列表失败', error);
    message.error('获取订单列表失败');
    data.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

const handleTabChange = (key: string) => {
  activeTab.value = key;
  pagination.current = 1;
  fetchOrders();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchOrders();
};

const showOrderDetail = (record: ExtendedOrderVO) => {
  currentOrder.value = record;
  orderDetailVisible.value = true;
};

const closeOrderDetail = () => {
  orderDetailVisible.value = false;
  currentOrder.value = null;
};

const handleShip = (id: number) => {
  // console.log(`准备发货，订单ID: ${id}`);
  
  // 查找订单
  const orderItem = data.value.find(item => item.id === id);
  if (!orderItem) {
    // console.error(`找不到ID为${id}的订单`);
    message.error('找不到对应的订单');
    return;
  }
  
  // console.log('找到订单信息:', orderItem);
  
  // 检查订单号
  if (!orderItem.orderNo) {
    // console.error('订单号不存在:', orderItem);
    message.error('订单号不存在');
    return;
  }
  
  // 存储订单号
  shipOrderNo.value = orderItem.orderNo;
  shipConfirmVisible.value = true;
};

const confirmShip = async () => {
  if (!shipOrderNo.value) {
    // console.error('订单号不存在');
    message.error('订单号不存在');
    return;
  }
  
  // console.log(`开始发货，订单号: ${shipOrderNo.value}`);
  
  try {
    // 关闭模态框并显示加载状态
    shipConfirmVisible.value = false;
    loading.value = true;
    
    // 调用发货API
    const response = await shipOrder(shipOrderNo.value);
    // console.log('发货API响应:', response);
    
    // 处理响应
    if (response === true || (response && typeof response === 'object' && response.code === 200)) {
      message.success('发货成功');
      // 如果当前是详情模态框，关闭它
      if (orderDetailVisible.value) {
        orderDetailVisible.value = false;
      }
      // 刷新订单列表
      await fetchOrders();
    } else {
      const errorMsg = response && typeof response === 'object' ? response.message : '发货失败，请重试';
      // console.error('发货失败，API返回:', errorMsg);
      message.error(errorMsg);
    }
  } catch (error) {
    // console.error('发货请求失败:', error);
    const errorMsg = error instanceof Error ? error.message : '发货失败，请重试';
    message.error(errorMsg);
  } finally {
    loading.value = false;
    shipOrderNo.value = null;
  }
};

const getStatusText = (status: string | undefined) => {
  if (!status) return '未知状态';
  
  switch (status) {
    case '0': return '已取消';
    case '1': return '待发货';
    case '2': return '已发货';
    case '3': return '已完成';
    case '4': return '已退款';
    default: return '未知状态';
  }
};

const getStatusType = (status: string | undefined) => {
  if (!status) return 'default';
  
  switch (status) {
    case '0': return 'default';
    case '1': return 'warning';
    case '2': return 'processing';
    case '3': return 'success';
    case '4': return 'error';
    default: return 'default';
  }
};

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.seller-orders {
  padding: 20px;
}

.orders {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.order-action {
  margin-top: 20px;
  text-align: right;
}
</style> 