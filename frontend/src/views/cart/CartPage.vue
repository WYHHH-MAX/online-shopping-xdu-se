<template>
  <div class="cart-page">
    <div class="content-wrapper">
      <h1 class="page-title">My Cart</h1>
      
      <div v-if="cartItems.length === 0" class="empty-cart">
        <a-empty description="The cart is empty">
          <template #extra>
            <a-button type="primary" @click="goToHome">Go shopping</a-button>
          </template>
        </a-empty>
      </div>
      
      <template v-else>
        <div class="cart-header">
          <div class="header-item checkbox">
            <a-checkbox
              :checked="allSelected"
              :indeterminate="indeterminate"
              @change="handleSelectAll"
            >Select all</a-checkbox>
          </div>
          <div class="header-item product">Product information</div>
          <div class="header-item price">unit price</div>
          <div class="header-item quantity">quantity</div>
          <div class="header-item subtotal">amount</div>
          <div class="header-item actions">operate</div>
        </div>
        
        <div class="cart-items">
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <div class="item-checkbox">
              <a-checkbox
                :checked="item.selected"
                @change="(e: any) => handleSelectItem(item.id, e.target.checked)"
              ></a-checkbox>
            </div>
            <div class="item-product">
              <img :src="getImageUrl(item.productImage)" :alt="item.productName" class="product-image">
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
              </div>
            </div>
            <div class="item-price">¥{{ item.price }}</div>
            <div class="item-quantity">
              <a-input-number
                v-model:value="item.quantity"
                :min="1"
                :max="item.stock"
                @change="(value: number) => handleQuantityChange(item.id, value)"
              />
            </div>
            <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            <div class="item-actions">
              <a-button type="link" danger @click="handleDelete(item.id)">delete</a-button>
            </div>
          </div>
        </div>
        
        <div class="cart-footer">
          <div class="selected-info">
            Selected <span class="selected-count">{{ selectedCount }}</span> products
          </div>
          <div class="total-info">
            total: <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <a-button type="primary" :disabled="selectedCount === 0" @click="handleCheckout">
            Go to checkout
          </a-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getCartItems } from '@/api/cart'
import { updateCartItem } from '@/api/cart'
import { deleteCartItem } from '@/api/cart'
import { selectCartItem } from '@/api/cart'
import { selectAllCartItems } from '@/api/cart'
import { selectCartItems } from '@/api/cart'
import type { CartItemVO } from '@/types/cart'
import { getImageUrl } from '@/utils/imageUtil'

const router = useRouter()
const cartItems = ref<CartItemVO[]>([])
const loading = ref(false)

// 计算属性
const selectedItems = computed(() => cartItems.value.filter(item => item.selected))
const selectedCount = computed(() => selectedItems.value.length)
const allSelected = computed(() => cartItems.value.length > 0 && selectedItems.value.length === cartItems.value.length)
const indeterminate = computed(() => selectedItems.value.length > 0 && selectedItems.value.length < cartItems.value.length)
const totalPrice = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0)
})

// 加载购物车数据
const loadCartItems = async () => {
  loading.value = true
  try {
    const result = await getCartItems()
    // console.log('获取购物车数据:', result)
    
    // 处理不同的数据格式
    if (result && Array.isArray(result)) {
      // 如果直接返回数组
      cartItems.value = result
    } else if (result && (result as any).data && Array.isArray((result as any).data)) {
      // 如果数据在data字段中
      cartItems.value = (result as any).data
    } else if (result && (result as any).list && Array.isArray((result as any).list)) {
      // 如果数据在list字段中
      cartItems.value = (result as any).list
    } else {
      console.error('购物车数据格式不正确:', result)
      cartItems.value = []
      message.error('获取购物车数据格式有误')
    }
  } catch (error) {
    // console.error('获取购物车数据失败:', error)
    message.error('获取购物车数据失败')
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

// 更新商品数量
const handleQuantityChange = async (id: number, quantity: number) => {
  try {
    await updateCartItem(id, quantity)
    message.success('更新数量成功')
  } catch (error) {
    // console.error('更新数量失败:', error)
    message.error('更新数量失败')
    // 失败时重新加载数据
    loadCartItems()
  }
}

// 删除商品
const handleDelete = async (id: number) => {
  try {
    await deleteCartItem(id)
    message.success('删除成功')
    cartItems.value = cartItems.value.filter(item => item.id !== id)
  } catch (error) {
    // console.error('删除失败:', error)
    message.error('删除失败')
  }
}

// 选择/取消选择商品
const handleSelectItem = async (id: number, selected: boolean) => {
  try {
    await selectCartItem(id, selected)
    // 更新本地状态
    const item = cartItems.value.find(item => item.id === id)
    if (item) {
      item.selected = selected
    }
  } catch (error) {
    // console.error('选择商品失败:', error)
    message.error('选择商品失败')
    loadCartItems()
  }
}

// 全选/取消全选
const handleSelectAll = async (e: any) => {
  const selected = e.target.checked
  try {
    await selectAllCartItems(selected)
    // 更新本地状态
    cartItems.value.forEach(item => {
      item.selected = selected
    })
  } catch (error) {
    // console.error('全选/取消全选失败:', error)
    message.error('操作失败')
    loadCartItems()
  }
}

// 批量选择商品
const handleSelectBatch = async (ids: number[]) => {
  if (!ids || ids.length === 0) return
  
  try {
    await selectCartItems(ids)
    // 更新本地状态
    ids.forEach(id => {
      const item = cartItems.value.find(item => item.id === id)
      if (item) {
        item.selected = true
      }
    })
    message.success('批量选择成功')
  } catch (error) {
    // console.error('批量选择商品失败:', error)
    message.error('批量选择失败')
    loadCartItems()
  }
}

// 去结算
const handleCheckout = () => {
  if (selectedCount.value === 0) {
    message.warning('请至少选择一件商品')
    return
  }
  
  const selectedIds = selectedItems.value.map(item => item.id)
  router.push({
    path: '/checkout',
    query: {
      items: selectedIds.join(',')
    }
  })
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

onMounted(() => {
  loadCartItems()
})
</script>

<style scoped>
.cart-page {
  padding: 24px;
}

.content-wrapper {
  background: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.page-title {
  margin-bottom: 24px;
  font-weight: 500;
  color: #333;
}

.empty-cart {
  padding: 60px 0;
  text-align: center;
}

.cart-header {
  display: flex;
  background-color: #f5f5f5;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  font-weight: 500;
}

.header-item {
  text-align: center;
}

.header-item.checkbox {
  flex: 0 0 60px;
}

.header-item.product {
  flex: 1;
  text-align: left;
}

.header-item.price,
.header-item.quantity,
.header-item.subtotal {
  flex: 0 0 120px;
}

.header-item.actions {
  flex: 0 0 100px;
}

.cart-items {
  margin-bottom: 24px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.item-checkbox {
  flex: 0 0 60px;
  display: flex;
  justify-content: center;
}

.item-product {
  flex: 1;
  display: flex;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  margin-bottom: 8px;
  color: #333;
}

.item-price,
.item-quantity,
.item-subtotal {
  flex: 0 0 120px;
  text-align: center;
}

.item-subtotal {
  font-weight: 500;
  color: #ff4d4f;
}

.item-actions {
  flex: 0 0 100px;
  text-align: center;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.selected-info {
  margin-right: 24px;
}

.selected-count {
  color: #ff4d4f;
  font-weight: 500;
}

.total-info {
  margin-right: 24px;
  font-size: 16px;
}

.total-price {
  color: #ff4d4f;
  font-weight: 500;
  font-size: 18px;
}
</style> 