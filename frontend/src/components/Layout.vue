<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo">spring</div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
      >
        <a-menu-item key="home" @click="router.push('/')">
          <home-outlined /> Home
        </a-menu-item>
        <a-menu-item key="cart" @click="router.push('/cart')">
          <shopping-cart-outlined /> Shopping cart
        </a-menu-item>
        <a-menu-item key="profile" @click="router.push('/profile')">
          <user-outlined /> Personal Center
        </a-menu-item>
        <a-menu-item v-if="userStore.isLoggedIn() && userStore.role === 0" key="apply-seller" @click="router.push('/apply-seller')">
          <shop-outlined /> Register as a seller
        </a-menu-item>
        <a-menu-item v-if="userStore.role === 1" key="seller" @click="router.push('/seller')">
          <shop-outlined /> Store management
        </a-menu-item>
        <!-- <a-menu-item>
          <span style="color: yellow">current: {{ userStore.role }}</span>
        </a-menu-item> -->
        <a-menu-item key="logout" @click="handleLogout">
          <logout-outlined /> Sign out
        </a-menu-item>
      </a-menu>
    </a-layout-header>
    
    <a-layout-content class="content">
      <router-view></router-view>
    </a-layout-content>
    
    <a-layout-footer class="footer">
      Online Shop ©2024 Created by Your Name
    </a-layout-footer>
  </a-layout>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  HomeOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  LogoutOutlined,
  ShopOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCurrentUser, refreshCurrentUser } from '@/api/user'
import { ApiResponse } from '@/types/auth'

const router = useRouter()
const userStore = useUserStore()
const selectedKeys = ref<string[]>(['home'])

// 添加调试信息
// console.log('Layout挂载 - 当前用户角色:', userStore.role, '从localStorage:', localStorage.getItem('role'))

onMounted(async () => {
  console.log('Layout组件已挂载')
  
  // 从localStorage初始化角色，防止API调用失败
  const storedRole = localStorage.getItem('role')
  if (storedRole && userStore.role !== parseInt(storedRole)) {
    console.log('初始化时从localStorage设置角色:', storedRole)
    userStore.role = parseInt(storedRole)
  }
  
  // 立即刷新用户角色
  try {
    const userData = await refreshCurrentUser()
    if (userData) {
      console.log('用户信息刷新成功，当前角色:', userData.role)
      // 强制更新角色
      userStore.role = userData.role
      
      // 确保localStorage和store一致
      localStorage.setItem('role', String(userData.role))
    }
  } catch (err) {
    console.error('刷新用户角色失败:', err)
  }
})

const handleLogout = () => {
  userStore.clearUserInfo()
  message.success('Logout successful')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
}

.logo {
  color: white;
  font-size: 20px;
  font-weight: bold;
  margin-right: 30px;
}

.content {
  padding: 24px;
  background: #fff;
}

.footer {
  text-align: center;
  background: #f0f2f5;
}
</style> 