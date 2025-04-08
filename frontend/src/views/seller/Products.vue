<template>
  <div class="product-list">
    <div class="actions">
      <a-button type="primary" @click="goToAddProduct">Add a product</a-button>
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
        <template v-if="column.key === 'price'">
          ¥{{ record.price.toFixed(2) }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? 'Shelves' : 'Taken off the shelves' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'stock'">
          <a-tag :color="record.stock > 10 ? 'green' : 'red'">
            {{ record.stock }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a @click="goToEditProduct(record.id)">edit</a>
            <a-divider type="vertical" />
            <a-popconfirm
              title="Are you sure you want to delete this item?？"
              @confirm="handleDelete(record.id)"
            >
              <a class="danger">delete</a>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Table, Button, Modal, Form, Input, InputNumber, Select, Upload } from 'ant-design-vue';
import { getSellerProducts, deleteProduct } from '@/api/seller';
import { getImageUrl } from '@/utils/imageUtil';
import type { ProductVO } from '@/types/product';

const router = useRouter();
const loading = ref(false);
const products = ref<ProductVO[]>([]);
const total = ref(0);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `totally ${total} records`
});

const columns = [
  {
    title: 'image',
    key: 'image',
    width: 100
  },
  {
    title: 'name',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: 'price',
    key: 'price',
    sorter: true
  },
  {
    title: 'stock',
    key: 'stock',
    sorter: true
  },
  {
    title: 'sales',
    dataIndex: 'sales',
    key: 'sales',
    sorter: true
  },
  {
    title: 'status',
    key: 'status'
  },
  {
    title: 'action',
    key: 'action',
    width: 150
  }
];

const fetchProducts = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    };
    
    // console.log('获取商品列表，参数:', params);
    const res = await getSellerProducts(params);
    // console.log('获取到的商品数据:', res);
    
    if (!res.list || res.list.length === 0) {
      // console.warn('没有获取到商品数据');
      products.value = [];
      pagination.total = 0;
      return;
    }
    
    // 处理商品数据，确保图片正确显示
    const processedProducts = res.list.map((product: any) => ({
      ...product,
      mainImage: product.mainImage ? getImageUrl(product.mainImage) : '/images/placeholder.jpg',
      images: Array.isArray(product.images) 
        ? product.images.map((img: any) => getImageUrl(img))
        : []
    }));
    
    // console.log('处理后的商品数据:', processedProducts);
    
    products.value = processedProducts;
    pagination.total = res.total;
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

const goToAddProduct = () => {
  router.push('/seller/products/add');
};

const goToEditProduct = (id: number) => {
  router.push(`/seller/products/edit/${id}`);
};

const handleDelete = async (id: number) => {
  try {
    await deleteProduct(id);
    message.success('删除成功');
    fetchProducts();
  } catch (error: any) {
    message.error('删除失败: ' + error.message);
  }
};

onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
.product-list {
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

.danger {
  color: #ff4d4f;
}
</style> 