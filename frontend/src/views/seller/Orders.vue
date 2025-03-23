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
                type="link" 
                size="small" 
                style="color: #1890ff;" 
                @click="handleShip(Number(record.id))"
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
          <a-descriptions-item label="订单号" :span="3">{{ currentOrder?.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="下单时间" :span="3">{{ currentOrder?.createTime }}</a-descriptions-item>
          <a-descriptions-item label="订单金额" :span="1">¥{{ currentOrder?.totalAmount }}</a-descriptions-item>
          <a-descriptions-item label="状态" :span="2">
            <a-tag :color="getStatusType(currentOrder?.status)">
              {{ getStatusText(currentOrder?.status) }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
        
        <a-divider />
        
        <a-table
          :columns="productColumns"
          :data-source="currentOrder?.products"
          :pagination="false"
          row-key="id"
        />
        
        <div class="order-action" v-if="currentOrder?.status === '1'">
          <a-button type="primary" @click="handleShip(Number(currentOrder.id))">发货</a-button>
        </div>
      </a-modal>
      
      <a-modal
        v-model:visible="shipConfirmVisible"
        title="确认发货"
        :footer="[
          { text: '取消', onClick: () => shipConfirmVisible = false },
          { text: '确认发货', type: 'primary', onClick: confirmShip }
        ]"
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
import type { OrderVO, OrderProductVO } from '@/types/order';

const tabs = ref([
  { key: '0', name: '全部订单' },
  { key: '1', name: '待发货' },
  { key: '2', name: '已发货' },
  { key: '3', name: '已完成' }
]);

const activeTab = ref('0');
const loading = ref(false);
const data = ref<OrderVO[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

const currentOrder = ref<OrderVO | null>(null);
const orderDetailVisible = ref(false);
const shipConfirmVisible = ref(false);
const shipOrderId = ref<number | null>(null);

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
    sorter: true
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
    const response = await getSellerOrders({
      status: activeTab.value === '0' ? undefined : activeTab.value,
      page: pagination.current,
      size: pagination.pageSize
    });
    data.value = response.records;
    pagination.total = response.total;
  } catch (error) {
    console.error('获取订单列表失败', error);
    message.error('获取订单列表失败');
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

const showOrderDetail = (record: OrderVO) => {
  currentOrder.value = record;
  orderDetailVisible.value = true;
};

const closeOrderDetail = () => {
  orderDetailVisible.value = false;
  currentOrder.value = null;
};

const handleShip = (id: number) => {
  shipOrderId.value = id;
  shipConfirmVisible.value = true;
};

const confirmShip = async () => {
  if (!shipOrderId.value) return;
  
  try {
    const currentOrder = data.value.find(o => o.id === shipOrderId.value);
    if (!currentOrder?.orderNo) {
      message.error('订单号不存在');
      return;
    }
    
    await shipOrder(currentOrder.orderNo);
    message.success('发货成功');
    fetchOrders();
  } catch (error) {
    console.error('发货失败', error);
    message.error('发货失败');
  } finally {
    shipConfirmVisible.value = false;
    shipOrderId.value = null;
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