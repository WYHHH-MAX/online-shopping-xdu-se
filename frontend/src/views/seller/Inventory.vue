<template>
  <div class="inventory">
    <div class="actions">
      <a-button type="primary" @click="batchUpdate">Bulk inventory update</a-button>
    </div>
    
    <a-table
      :columns="columns"
      :data-source="products"
      :pagination="pagination"
      :loading="loading"
      @change="handleTableChange"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'image'">
          <img :src="record.mainImage" alt="product-image" class="product-image" />
        </template>
        <template v-if="column.key === 'stock'">
          <a-input-number 
            v-model:value="stockMap[record.id]" 
            :min="0" 
            @change="(value: number) => handleStockChange(record.id, value)"
          />
        </template>
        <template v-if="column.key === 'stockStatus'">
          <a-tag :color="getStockStatusColor(record.stock)">
            {{ getStockStatusText(record.stock) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-button 
            type="primary" 
            size="small" 
            @click="updateStock(record.id)" 
            :disabled="!isStockChanged(record.id)"
          >
            update
          </a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Table, InputNumber, Button } from 'ant-design-vue';
import { getSellerProducts, updateProductStock, batchUpdateProductStock } from '@/api/seller';
import { getImageUrl } from '@/utils/imageUtil';
import type { ProductVO } from '@/types/product';

const loading = ref(false);
const products = ref<ProductVO[]>([]);
const stockMap = ref<Record<number, number>>({});
const originalStockMap = ref<Record<number, number>>({});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条记录`
});

const columns = [
  {
    title: '图片',
    key: 'image',
    width: 100
  },
  {
    title: '商品名称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '当前库存',
    key: 'stock'
  },
  {
    title: '库存状态',
    key: 'stockStatus'
  },
  {
    title: '操作',
    key: 'action',
    width: 100
  }
];

const fetchProducts = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    };
    
    // console.log('获取库存数据，参数:', params);
    const res = await getSellerProducts(params);
    // console.log('获取到的库存数据:', res);
    
    if (!res.list || res.list.length === 0) {
      console.warn('没有获取到商品数据');
      products.value = [];
      pagination.total = 0;
      return;
    }
    
    // 处理商品数据，确保图片正确显示
    const processedProducts = res.list.map(product => ({
      ...product,
      mainImage: product.mainImage ? getImageUrl(product.mainImage) : '/images/placeholder.jpg',
      images: Array.isArray(product.images) 
        ? product.images.map(img => getImageUrl(img))
        : []
    }));
    
    // console.log('处理后的库存数据:', processedProducts);
    
    products.value = processedProducts;
    pagination.total = res.total;
    
    // 初始化库存映射
    processedProducts.forEach((product: ProductVO) => {
      stockMap.value[product.id] = product.stock;
      originalStockMap.value[product.id] = product.stock;
    });
  } catch (error: any) {
    // console.error('获取商品列表失败:', error);
    message.error('获取商品列表失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchProducts();
};

const handleStockChange = (productId: number, value: number) => {
  stockMap.value[productId] = value;
};

const isStockChanged = (productId: number) => {
  return stockMap.value[productId] !== originalStockMap.value[productId];
};

const getStockStatusColor = (stock: number) => {
  if (stock <= 0) return 'red';
  if (stock <= 10) return 'orange';
  return 'green';
};

const getStockStatusText = (stock: number) => {
  if (stock <= 0) return '缺货';
  if (stock <= 10) return '库存紧张';
  return '库存充足';
};

const updateStock = async (productId: number) => {
  try {
    await updateProductStock(productId, stockMap.value[productId]);
    message.success('库存更新成功');
    
    // 更新原始库存
    originalStockMap.value[productId] = stockMap.value[productId];
    
    // 更新列表中的库存
    const index = products.value.findIndex(p => p.id === productId);
    if (index !== -1) {
      products.value[index].stock = stockMap.value[productId];
    }
  } catch (error: any) {
    message.error('库存更新失败: ' + error.message);
  }
};

const batchUpdate = async () => {
  // 找出所有已修改的库存
  const changedStocks: Record<number, number> = {};
  Object.keys(stockMap.value).forEach(key => {
    const productId = Number(key);
    if (isStockChanged(productId)) {
      changedStocks[productId] = stockMap.value[productId];
    }
  });
  
  if (Object.keys(changedStocks).length === 0) {
    message.info('没有需要更新的库存');
    return;
  }
  
  try {
    await batchUpdateProductStock(changedStocks);
    message.success('批量更新库存成功');
    
    // 更新原始库存
    Object.keys(changedStocks).forEach(key => {
      const productId = Number(key);
      originalStockMap.value[productId] = stockMap.value[productId];
      
      // 更新列表中的库存
      const index = products.value.findIndex(p => p.id === productId);
      if (index !== -1) {
        products.value[index].stock = stockMap.value[productId];
      }
    });
  } catch (error: any) {
    message.error('批量更新库存失败: ' + error.message);
  }
};

onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
.inventory {
  padding: 20px;
}

.actions {
  margin-bottom: 16px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}
</style> 