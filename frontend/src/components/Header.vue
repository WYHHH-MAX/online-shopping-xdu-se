<template>
  <a-layout-header class="header">
    <div class="logo">
      <router-link to="/">
        <img src="@/assets/logo.png" alt="Logo" class="logo-image" />
      </router-link>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      mode="horizontal"
      :style="{ lineHeight: '64px', flex: 1 }"
    >
      <a-menu-item key="home" @click="router.push('/')">
        <template #icon><HomeOutlined /></template>
        首页
      </a-menu-item>
      <a-menu-item key="category" @click="router.push('/category')">
        <template #icon><AppstoreOutlined /></template>
        分类
      </a-menu-item>
      <a-menu-item key="cart" @click="router.push('/cart')">
        <template #icon><ShoppingCartOutlined /></template>
        购物车
      </a-menu-item>
      <a-menu-item key="orders" @click="router.push('/orders')">
        <template #icon><OrderedListOutlined /></template>
        我的订单
      </a-menu-item>
      <a-menu-item key="profile" @click="router.push('/profile')">
        <template #icon><UserOutlined /></template>
        个人中心
      </a-menu-item>
      <a-menu-item v-if="userStore.isLoggedIn() && userStore.role === 0" key="apply-seller" @click="router.push('/apply-seller')">
        <template #icon><ShopOutlined /></template>
        注册卖家
      </a-menu-item>
    </a-menu>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  HomeOutlined,
  AppstoreOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  OrderedListOutlined,
  ShopOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const selectedKeys = ref<string[]>(['home'])

watch(() => route.path, (newPath) => {
  const key = newPath.split('/')[1] || 'home'
  selectedKeys.value = [key]
}, { immediate: true })
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.logo {
  margin-right: 48px;
  min-width: 120px;
  display: flex;
  align-items: center;
}

.logo a {
  display: flex;
  align-items: center;
  height: 64px;
}

.logo-image {
  height: 32px;
  width: auto;
  object-fit: contain;
}

:deep(.ant-menu) {
  margin-left: auto;
}

:deep(.ant-menu-item) {
  padding: 0 24px;
}
</style> 