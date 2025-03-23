<template>
  <div class="product-list">
    <div class="actions">
      <a-button type="primary" @click="goToAddProduct">添加商品</a-button>
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
          <img :src="record.mainImage" alt="商品图片" class="product-image" />
        </template>
        <template v-if="column.key === 'price'">
          ¥{{ record.price.toFixed(2) }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '上架' : '下架' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'stock'">
          <a-tag :color="record.stock > 10 ? 'green' : 'red'">
            {{ record.stock }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a @click="goToEditProduct(record.id)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm
              title="确定要删除这个商品吗？"
              @confirm="handleDelete(record.id)"
            >
              <a class="danger">删除</a>
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
import { message } from 'ant-design-vue';
import { getSellerProducts, deleteProduct } from '@/api/seller';
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
    title: '价格',
    key: 'price',
    sorter: true
  },
  {
    title: '库存',
    key: 'stock',
    sorter: true
  },
  {
    title: '销量',
    dataIndex: 'sales',
    key: 'sales',
    sorter: true
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

const fetchProducts = async () => {
  loading.value = true;
  try {
    const res = await getSellerProducts({
      page: pagination.current,
      size: pagination.pageSize
    });
    products.value = res.list;
    total.value = res.total;
    pagination.total = res.total;
  } catch (error: any) {
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