<template>
  <div class="product-list">
    <a-row :gutter="[16, 16]">
      <a-col :span="6" v-for="product in products" :key="product.id">
        <a-card hoverable @click="handleProductClick(product.id)">
          <template #cover>
            <img :src="getImageUrl(product.mainImage)" :alt="product.name" @error="handleImageError" />
          </template>
          <a-card-meta :title="product.name">
            <template #description>
              <div class="product-price">¥{{ product.price }}</div>
              <div class="product-sales">Sales: {{ product.sales }}</div>
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
    </a-row>
    <div class="pagination" v-if="total > 0">
      <a-pagination
        v-model:current="current"
        :total="total"
        :pageSize="pageSize"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { getImageUrl, handleImageError } from '@/utils/imageUtil';

const props = defineProps({
  products: {
    type: Array,
    required: true
  },
  total: {
    type: Number,
    required: true
  }
});

const router = useRouter();
const current = ref(1);
const pageSize = ref(12);

const emit = defineEmits(['page-change']);

const handleProductClick = (productId) => {
  router.push(`/product/${productId}`);
};

const handlePageChange = (page) => {
  current.value = page;
  emit('page-change', page);
};
</script>

<style scoped>
.product-list {
  padding: 20px;
}

.product-price {
  color: #f5222d;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}

.product-sales {
  color: #8c8c8c;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

img {
  height: 200px;
  object-fit: cover;
}
</style> 