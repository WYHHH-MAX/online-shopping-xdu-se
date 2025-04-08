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
import { getSalesAnalytics, exportFinancialReport } from '@/api/seller';
import type { SalesAnalyticsData } from '@/types/seller';

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

// 格式化数字显示
const formatNumber = (num?: number): string => {
  if (num === undefined || num === null) return '0';
  
  return num.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};

// 获取销售数据
const fetchSalesData = async () => {
  try {
    if (!dateRange.value || dateRange.value.length !== 2) {
      return;
    }
    
    const startDate = dateRange.value[0].format('YYYY-MM-DD');
    const endDate = dateRange.value[1].format('YYYY-MM-DD');
    
    const response = await getSalesAnalytics({
      startDate,
      endDate,
      period: period.value,
    });
    
    // 更新销售数据
    if (response && typeof response === 'object') {
      if (response.overview) {
        salesData.overview = response.overview;
      }
      
      if (Array.isArray(response.salesByTime)) {
        salesData.salesByTime = response.salesByTime;
      }
      
      if (Array.isArray(response.salesByCategory)) {
        salesData.salesByCategory = response.salesByCategory;
      }
      
      if (Array.isArray(response.topProducts)) {
        salesData.topProducts = response.topProducts;
      }
      
      // 更新图表
      nextTick(() => {
        renderSalesTrendChart();
        renderCategoryPieChart();
      });
    }
  } catch (error: any) {
    message.error('Failed to fetch sales data: ' + error.message);
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
</style> 