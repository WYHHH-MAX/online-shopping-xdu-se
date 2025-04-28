<template>
  <div class="sales-analytics">
    <div class="page-header">
      <h1>Sales Data Analytics</h1>
      
      <div class="date-filter">
        <a-range-picker 
          :value="dateRange" 
          @change="handleDateChange" 
          :disabled-date="disabledDate"
          format="YYYY-MM-DD"
        />
        <a-select 
          v-model:value="period" 
          style="width: 120px; margin-left: 10px;"
          @change="fetchSalesData"
        >
          <a-select-option value="day">Daily</a-select-option>
          <a-select-option value="week">Weekly</a-select-option>
          <a-select-option value="month">Monthly</a-select-option>
        </a-select>
      </div>
    </div>
    
    <a-row :gutter="16" class="stat-cards">
      <a-col :span="8">
        <a-card>
          <template #title>Total Sales</template>
          <div class="stat-value">¥{{ formatNumber(salesData.overview?.totalSales) }}</div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card>
          <template #title>Total Orders</template>
          <div class="stat-value">{{ formatNumber(salesData.overview?.totalOrders) }}</div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card>
          <template #title>Average Order Value</template>
          <div class="stat-value">¥{{ formatNumber(salesData.overview?.averageOrderValue) }}</div>
        </a-card>
      </a-col>
    </a-row>
    
    <a-row :gutter="16" class="chart-row">
      <a-col :span="16">
        <a-card title="Sales Trend">
          <div ref="salesTrendChart" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="Sales by Category">
          <div ref="categoryPieChart" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>
    
    <a-card title="Order Summary" class="order-summary-section">
      <div class="card-subtitle">Recent completed orders in selected date range</div>
      
      <a-table
        :columns="orderColumns"
        :data-source="ordersData"
        :pagination="{ pageSize: 5, showTotal: (total: number) => `Total ${total} orders` }"
        :loading="ordersLoading"
        row-key="id"
        :locale="{ emptyText: 'No orders in selected date range' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-if="column.key === 'amount'">
            ¥{{ formatNumber(record.totalAmount) }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusType(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button 
              type="link" 
              size="small" 
              @click="showOrderDetail(record)"
            >
              Find out more
            </a-button>
          </template>
        </template>
      </a-table>
      
      <!-- Order Detail Modal -->
      <a-modal
        v-model:visible="orderDetailVisible"
        title="Order Details"
        width="800px"
        :footer="null"
      >
        <a-descriptions bordered :column="3">
          <a-descriptions-item label="Order Number" :span="3">{{ currentOrder ? currentOrder.orderNo : 'No order number' }}</a-descriptions-item>
          <a-descriptions-item label="Order Time" :span="3">{{ currentOrder ? formatDate(currentOrder.createTime) : 'No order time' }}</a-descriptions-item>
          <a-descriptions-item label="User" :span="1">{{ currentOrder?.username || 'Unknown user' }}</a-descriptions-item>
          <a-descriptions-item label="Order Amount" :span="1">{{ currentOrder ? `¥${formatNumber(currentOrder.totalAmount)}` : '¥0.00' }}</a-descriptions-item>
          <a-descriptions-item label="Status" :span="1">
            <a-tag :color="currentOrder ? getStatusType(currentOrder.status) : 'default'">
              {{ currentOrder ? getStatusText(currentOrder.status) : 'Unknown status' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="Payment Method" :span="3">{{ getPaymentMethodText(currentOrder?.paymentMethod) }}</a-descriptions-item>
          <a-descriptions-item label="Receiver Phone" :span="1">{{ currentOrder?.phone || 'Not available' }}</a-descriptions-item>
          <a-descriptions-item label="Receiver Address" :span="2">{{ currentOrder?.location || 'Not available' }}</a-descriptions-item>
        </a-descriptions>
        
        <a-divider />
        
        <a-table
          :columns="orderProductColumns"
          :data-source="currentOrder?.products || []"
          :pagination="false"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'price'">
              ¥{{ formatNumber(record.price) }}
            </template>
            <template v-if="column.key === 'subtotal'">
              ¥{{ formatNumber(record.price * record.quantity) }}
            </template>
          </template>
        </a-table>
      </a-modal>
    </a-card>
    
    <a-card title="Top Selling Products" class="top-products">
      <a-table
        :columns="topProductColumns"
        :data-source="salesData.topProducts || []"
        :pagination="false"
        row-key="productId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            ¥{{ formatNumber(record.salesAmount) }}
          </template>
        </template>
      </a-table>
    </a-card>
    
    <a-card title="Export Financial Report" class="export-section">
      <a-form layout="inline">
        <a-form-item label="Report Type">
          <a-select v-model:value="reportType" style="width: 120px;">
            <a-select-option value="daily">Daily</a-select-option>
            <a-select-option value="weekly">Weekly</a-select-option>
            <a-select-option value="monthly">Monthly</a-select-option>
            <a-select-option value="custom">Custom Range</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item>
          <a-button type="primary" :loading="exportLoading" @click="exportReport">
            Export CSV Report
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { getSalesAnalytics, exportFinancialReport, getSellerOrders } from '@/api/seller';
import type { SalesAnalyticsData } from '@/types/seller';
import type { OrderVO } from '@/types/order';
import { getImageUrl } from '@/utils/imageUtil';

// Utility functions for formatting
const formatDate = (timestamp: number | string) => {
  if (!timestamp) return 'N/A';
  return dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm:ss');
};

const formatNumber = (num: number | string) => {
  if (num === undefined || num === null) return '0.00';
  return Number(num).toFixed(2);
};

// 使用全局变量来访问CDN引入的echarts
declare global {
  interface Window {
    echarts: any;
  }
}

// 在head中插入echarts的CDN脚本
const loadEcharts = () => {
  const script = document.createElement('script');
  script.src = 'https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js';
  script.async = true;
  document.head.appendChild(script);
  
  // 返回一个Promise，当脚本加载完成时解析
  return new Promise<void>((resolve, reject) => {
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Failed to load ECharts from CDN'));
  });
};

// 图表引用
const salesTrendChart = ref<HTMLElement | null>(null);
const categoryPieChart = ref<HTMLElement | null>(null);

// 销售数据
const salesData = reactive<SalesAnalyticsData>({
  overview: {
    totalSales: 0,
    totalOrders: 0,
    averageOrderValue: 0,
  },
  salesByTime: [],
  salesByCategory: [],
  topProducts: [],
});

// 日期范围和周期
const dateRange = ref<[Dayjs, Dayjs] | null>([
  dayjs().subtract(30, 'day'),
  dayjs(),
]);
const period = ref<string>('day');

// 报表导出设置
const reportType = ref<string>('monthly');
const exportLoading = ref<boolean>(false);

// 热销商品表格列
const topProductColumns = [
  {
    title: 'Product Name',
    dataIndex: 'productName',
    key: 'productName',
  },
  {
    title: 'Sales Amount',
    key: 'amount',
  },
  {
    title: 'Units Sold',
    dataIndex: 'salesCount',
    key: 'salesCount',
    sorter: (a: any, b: any) => a.salesCount - b.salesCount,
  },
];

// 禁用未来日期
const disabledDate = (current: Dayjs) => {
  return current && current > dayjs().endOf('day');
};

// 日期范围变化处理
const handleDateChange = (dates: [Dayjs, Dayjs] | null) => {
  dateRange.value = dates;
  fetchSalesData();
};

// Variables for orders display
const ordersData = ref<any[]>([]);
const orderDetailVisible = ref(false);
const currentOrder = ref<any>(null);
const ordersLoading = ref(false);

// Order columns for the table
const orderColumns = [
  {
    title: 'Order No',
    dataIndex: 'orderNo',
    key: 'orderNo',
  },
  {
    title: 'User',
    dataIndex: 'username',
    key: 'username',
    customRender: ({ text }: { text: string }) => text || 'Unknown User',
  },
  {
    title: 'Order Time',
    dataIndex: 'createTime',
    key: 'orderTime',
  },
  {
    title: 'Amount',
    dataIndex: 'totalAmount',
    key: 'amount',
  },
  {
    title: 'Status',
    key: 'status',
  },
  {
    title: 'Actions',
    key: 'action',
  },
];

// Order product columns for the detail modal
const orderProductColumns = [
  {
    title: 'Product',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: 'Unit Price',
    dataIndex: 'price',
    key: 'price',
  },
  {
    title: 'Quantity',
    dataIndex: 'quantity',
    key: 'quantity',
  },
  {
    title: 'Subtotal',
    key: 'subtotal',
  },
];

// Helper functions for order status and payment method
const getStatusType = (status: string | number) => {
  const statusStr = String(status);
  const statusMap: Record<string, string> = {
    '0': 'default',    // Pending payment
    '1': 'processing', // Paid, pending shipment
    '2': 'warning',    // Shipped
    '3': 'success',    // Completed
    '4': 'error',      // Canceled
    '5': 'default',    // Refunded
  };
  return statusMap[statusStr] || 'default';
};

const getStatusText = (status: string | number) => {
  const statusStr = String(status);
  const statusMap: Record<string, string> = {
    '0': 'Pending Payment',
    '1': 'Paid, Pending Shipment',
    '2': 'Shipped',
    '3': 'Completed',
    '4': 'Canceled',
    '5': 'Refunded',
  };
  return statusMap[statusStr] || 'Unknown';
};

const getPaymentMethodText = (method: string | number) => {
  if (!method) return 'Unknown';
  const methodStr = String(method);
  const methodMap: Record<string, string> = {
    '1': 'Alipay',
    '2': 'WeChat Pay',
    '3': 'Bank Card',
    '4': 'Cash on Delivery',
  };
  return methodMap[methodStr] || 'Unknown';
};

// Function to fetch orders
const fetchOrders = async () => {
  try {
    if (!dateRange.value || dateRange.value.length !== 2) {
      return;
    }
    
    ordersLoading.value = true;
    
    const startDate = dateRange.value[0].format('YYYY-MM-DD');
    const endDate = dateRange.value[1].format('YYYY-MM-DD');
    
    console.log('Fetching orders for date range:', startDate, 'to', endDate);
    
    // 获取所有订单，前端再根据日期筛选
    const response = await getSellerOrders({
      page: 1,
      size: 100  // 获取更多订单，以便筛选
    });
    
    console.log('Order API response:', response);
    
    if (response && response.data && response.data.list) {
      // 转换日期范围为时间戳，方便比较
      const startTimestamp = dateRange.value[0].startOf('day').valueOf();
      const endTimestamp = dateRange.value[1].endOf('day').valueOf();
      
      console.log('Filtering orders between timestamps:', startTimestamp, 'to', endTimestamp);
      
      // 处理每个订单，并筛选日期范围内的订单
      const filteredOrders = response.data.list.filter((order: any) => {
        // 将订单时间转换为时间戳进行比较
        const orderTime = new Date(order.createTime).getTime();
        return orderTime >= startTimestamp && orderTime <= endTimestamp;
      });
      
      console.log('Filtered orders count:', filteredOrders.length);
      
      // 处理订单数据，确保格式正确
      const processedOrders = filteredOrders.map((order: any, index: number) => {
        // 为每个订单分配唯一ID
        const id = index + 1;
        
        return {
          ...order,
          id: id,
          // 确保orderNo为字符串
          orderNo: String(order.orderNo || ''),
          // 确保status为字符串
          status: String(order.status || '0'),
          // 设置默认用户名
          username: order.username || 'Unknown User',
          // 处理商品图片
          products: Array.isArray(order.products) ? order.products.map((product: any) => ({
            ...product,
            image: product.image 
              ? (product.image.startsWith('http') ? product.image : getImageUrl(product.image)) 
              : '/images/placeholder.jpg'
          })) : []
        };
      });
      
      console.log('Processed orders:', processedOrders);
      
      // 按订单时间降序排序（最新的订单排在前面）
      ordersData.value = processedOrders.sort((a: any, b: any) => {
        const timeA = new Date(a.createTime).getTime();
        const timeB = new Date(b.createTime).getTime();
        return timeB - timeA;
      });
    } else {
      console.warn('Invalid order data format:', response);
      ordersData.value = [];
    }
  } catch (error: any) {
    console.error('Failed to fetch orders:', error);
    message.error('Failed to load order data');
    ordersData.value = [];
  } finally {
    ordersLoading.value = false;
  }
};

// Function to show order details
const showOrderDetail = (order: any) => {
  currentOrder.value = order;
  orderDetailVisible.value = true;
};

// Modified fetchSalesData to also fetch orders
const fetchSalesData = async () => {
  try {
    if (!dateRange.value || dateRange.value.length !== 2) {
      return;
    }
    
    // 设置加载状态
    ordersLoading.value = true;
    
    const startDate = dateRange.value[0].format('YYYY-MM-DD');
    const endDate = dateRange.value[1].format('YYYY-MM-DD');
    
    console.log('Fetching sales analytics data:', { startDate, endDate, period: period.value });
    
    const response = await getSalesAnalytics({
      startDate,
      endDate,
      period: period.value,
    });
    
    console.log('Sales analytics response:', response);
    
    // Update sales data
    if (response && typeof response === 'object') {
      // 处理销售概览数据
      if (response.overview) {
        salesData.overview = response.overview;
      }
      
      // 处理销售时间趋势数据
      if (Array.isArray(response.salesByTime)) {
        salesData.salesByTime = response.salesByTime;
      }
      
      // 处理分类销售数据
      if (Array.isArray(response.salesByCategory)) {
        salesData.salesByCategory = response.salesByCategory;
      }
      
      // 处理热销商品数据
      if (Array.isArray(response.topProducts)) {
        salesData.topProducts = response.topProducts;
      }
      
      // 处理最近订单数据
      if (Array.isArray(response.recentOrders)) {
        console.log('Recent orders from API:', response.recentOrders);
        
        // 转换日期范围为时间戳，方便筛选
        const startTimestamp = dateRange.value[0].startOf('day').valueOf();
        const endTimestamp = dateRange.value[1].endOf('day').valueOf();
        
        // 处理订单数据，确保格式正确，并且过滤日期范围
        const processedOrders = response.recentOrders
          .filter((order: any) => {
            // 确保订单在选择的日期范围内
            const orderTime = new Date(order.createTime).getTime();
            return orderTime >= startTimestamp && orderTime <= endTimestamp;
          })
          .map((order: any, index: number) => {
            return {
              ...order,
              id: index + 1,  // 生成唯一ID
              orderNo: String(order.orderNo || ''),
              status: String(order.status || '0'),
              username: order.username || 'Unknown User',
              products: Array.isArray(order.products) ? order.products.map((product: any) => ({
                ...product,
                image: product.image 
                  ? (product.image.startsWith('http') ? product.image : getImageUrl(product.image)) 
                  : '/images/placeholder.jpg'
              })) : []
            };
          });
        
        // 按订单时间降序排序
        ordersData.value = processedOrders.sort((a: any, b: any) => {
          const timeA = new Date(a.createTime).getTime();
          const timeB = new Date(b.createTime).getTime();
          return timeB - timeA;
        });
        
        console.log('Processed recent orders:', ordersData.value);
      } else {
        // 如果后端没有返回订单数据，则调用前端的获取订单方法
        console.log('No orders in response, fetching separately');
        await fetchOrders();
      }
      
      // 更新图表
      nextTick(() => {
        renderSalesTrendChart();
        renderCategoryPieChart();
      });
    }
  } catch (error: any) {
    console.error('Failed to fetch sales data:', error);
    message.error('Failed to fetch sales data: ' + error.message);
    
    // 尝试单独获取订单数据
    await fetchOrders();
  } finally {
    ordersLoading.value = false;
  }
};

// 渲染销售趋势图表
const renderSalesTrendChart = () => {
  if (!window.echarts || !salesTrendChart.value) return;
  
  const chart = window.echarts.init(salesTrendChart.value);
  
  const periods = salesData.salesByTime.map(item => item.period);
  const amounts = salesData.salesByTime.map(item => item.amount);
  const counts = salesData.salesByTime.map(item => item.orderCount);
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['Sales Amount', 'Order Count']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: periods,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: [
      {
        type: 'value',
        name: 'Sales Amount (¥)',
        position: 'left',
        axisLine: {
          show: true,
          lineStyle: {
            color: '#5470C6'
          }
        },
        axisLabel: {
          formatter: '{value} ¥'
        }
      },
      {
        type: 'value',
        name: 'Order Count',
        position: 'right',
        axisLine: {
          show: true,
          lineStyle: {
            color: '#91CC75'
          }
        },
        axisLabel: {
          formatter: '{value}'
        }
      }
    ],
    series: [
      {
        name: 'Sales Amount',
        type: 'bar',
        data: amounts,
        itemStyle: {
          color: '#5470C6'
        }
      },
      {
        name: 'Order Count',
        type: 'line',
        yAxisIndex: 1,
        data: counts,
        itemStyle: {
          color: '#91CC75'
        }
      }
    ]
  };
  
  option && chart.setOption(option);
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    chart.resize();
  });
};

// 渲染类别饼图
const renderCategoryPieChart = () => {
  if (!window.echarts || !categoryPieChart.value) return;
  
  const chart = window.echarts.init(categoryPieChart.value);
  
  const data = salesData.salesByCategory.map(item => ({
    name: item.category,
    value: item.amount
  }));
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: data.map(item => item.name)
    },
    series: [
      {
        name: 'Sales by Category',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  };
  
  option && chart.setOption(option);
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    chart.resize();
  });
};

// 导出报表
const exportReport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    message.warning('Please select a date range');
    return;
  }
  
  try {
    exportLoading.value = true;
    
    const startDate = dateRange.value[0].format('YYYY-MM-DD');
    const endDate = dateRange.value[1].format('YYYY-MM-DD');
    
    const response = await exportFinancialReport({
      startDate,
      endDate,
      reportType: reportType.value
    });
    
    // 创建下载链接
    const blob = new Blob([response], { 
      type: 'text/csv;charset=utf-8;' 
    });
    
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `financial_report_${startDate}_to_${endDate}.csv`;
    link.click();
    
    message.success('Report exported successfully');
  } catch (error: any) {
    message.error('Failed to export report: ' + error.message);
  } finally {
    exportLoading.value = false;
  }
};

// 监听周期变化
watch(period, () => {
  fetchSalesData();
});

// 组件挂载后初始化
onMounted(async () => {
  try {
    // 加载echarts
    await loadEcharts();
    // 获取销售数据
    fetchSalesData();
  } catch (error) {
    console.error('Failed to initialize:', error);
    message.error('Failed to load charts library. Please refresh the page and try again.');
  }
});
</script>

<style scoped>
.sales-analytics {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  text-align: center;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.top-products {
  margin-bottom: 20px;
}

.export-section {
  margin-bottom: 20px;
}

.order-summary-section {
  margin-bottom: 20px;
}

.card-subtitle {
  color: #888;
  font-size: 14px;
  margin-bottom: 16px;
}
</style> 