<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo">在线商城</div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
      >
        <a-menu-item key="home" @click="router.push('/')">
          <home-outlined /> 首页
        </a-menu-item>
        <a-menu-item key="cart" @click="router.push('/cart')">
          <shopping-cart-outlined /> 购物车
        </a-menu-item>
        <a-menu-item key="profile" @click="router.push('/profile')">
          <user-outlined /> 个人中心
        </a-menu-item>
        <a-menu-item key="logout" @click="handleLogout">
          <logout-outlined /> 退出登录
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  HomeOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const selectedKeys = ref<string[]>(['home'])

const handleLogout = () => {
  userStore.clearUserInfo()
  message.success('退出登录成功')
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