<template>
  <div class="cart">
    <h2>My Cart</h2>
    
    <!-- 购物车列表 -->
    <a-table
      :columns="columns"
      :data-source="cartItems"
      :pagination="false"
      :row-selection="rowSelection"
    >
      <!-- 商品信息 -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'product'">
          <div class="product-info">
            <img :src="record.image" :alt="record.name" class="product-image" />
            <span>{{ record.name }}</span>
          </div>
        </template>
        
        <!-- 数量 -->
        <template v-if="column.key === 'quantity'">
          <a-input-number
            v-model:value="record.quantity"
            :min="1"
            :max="record.stock"
            @change="(value: number) => handleQuantityChange(record, value)"
          />
        </template>
        
        <!-- 小计 -->
        <template v-if="column.key === 'subtotal'">
          <span class="price">¥{{ (record.price * record.quantity).toFixed(2) }}</span>
        </template>
        
        <!-- 操作 -->
        <template v-if="column.key === 'action'">
          <a-button type="link" @click="removeFromCart(record)">delete</a-button>
        </template>
      </template>
    </a-table>
    
    <!-- 结算区域 -->
    <div class="settlement">
      <div class="total">
        Selected {{ selectedRows.length }} products
        <span class="price">total:¥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <a-button type="primary" :disabled="!selectedRows.length" @click="handleCheckout">
        settlement
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { TableColumnType } from 'ant-design-vue'

interface CartItem {
  id: number
  name: string
  image: string
  price: number
  quantity: number
  stock: number
}

const columns: TableColumnType[] = [
  {
    title: '商品信息',
    key: 'product',
    width: '40%'
  },
  {
    title: '单价',
    dataIndex: 'price',
    key: 'price',
    width: '15%',
    customRender: ({ text }) => `¥${text.toFixed(2)}`
  },
  {
    title: '数量',
    key: 'quantity',
    width: '15%'
  },
  {
    title: '小计',
    key: 'subtotal',
    width: '15%'
  },
  {
    title: '操作',
    key: 'action',
    width: '15%'
  }
]

// 模拟购物车数据
const cartItems = ref<CartItem[]>([
  {
    id: 1,
    name: '示例商品1',
    image: 'https://placehold.co/100',
    price: 99.99,
    quantity: 1,
    stock: 10
  },
  {
    id: 2,
    name: '示例商品2',
    image: 'https://placehold.co/100',
    price: 199.99,
    quantity: 2,
    stock: 5
  }
])

const selectedRows = ref<CartItem[]>([])

const rowSelection = {
  onChange: (selectedRowKeys: number[], selected: CartItem[]) => {
    selectedRows.value = selected
  }
}

// 计算总价
const totalPrice = computed(() => {
  return selectedRows.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0)
})

// 修改数量
const handleQuantityChange = (item: CartItem, value: number) => {
  item.quantity = value
}

// 删除商品
const removeFromCart = (item: CartItem) => {
  const index = cartItems.value.findIndex(i => i.id === item.id)
  if (index > -1) {
    cartItems.value.splice(index, 1)
    message.success('删除成功')
  }
}

// 结算
const handleCheckout = () => {
  if (!selectedRows.value.length) {
    message.warning('请选择要结算的商品')
    return
  }
  // TODO: 跳转到结算页面
  message.success('正在跳转到结算页面...')
}
</script>

<style scoped>
.cart {
  padding: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.price {
  color: #f5222d;
  font-weight: bold;
}

.settlement {
  margin-top: 20px;
  padding: 20px;
  background: #f8f8f8;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
}

.total {
  font-size: 16px;
}

.total .price {
  margin-left: 10px;
  font-size: 20px;
}
</style> 