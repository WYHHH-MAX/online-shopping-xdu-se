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
        <a-menu-item v-if="userStore.role === 1" key="seller" @click="router.push('/seller')">
          <shop-outlined /> 商铺管理
        </a-menu-item>
<!--        <a-menu-item>-->
<!--          <span style="color: yellow">角色: {{ userStore.role }}</span>-->
<!--        </a-menu-item>-->
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

// 检查并刷新用户信息
const refreshUserRole = async () => {
  // console.log('开始刷新用户信息...')
  try {
    // 先记录当前状态
    // console.log('刷新前状态 - 角色:', userStore.role, 'localStorage角色:', localStorage.getItem('role'))
    
    // 使用新的集成函数直接刷新用户信息
    const userData = await refreshCurrentUser()
    
    if (userData) {
      // console.log('用户信息刷新成功，新角色:', userData.role)
      // console.log('刷新后的store角色:', userStore.role, '从localStorage:', localStorage.getItem('role'))
      
      // 确保用户store中的角色和localStorage一致
      if (userStore.role !== parseInt(localStorage.getItem('role') || '0')) {
        // console.log('手动同步角色信息')
        userStore.role = parseInt(localStorage.getItem('role') || '0')
      }
    } else {
      console.warn('刷新用户信息未返回数据，使用当前角色')
    }
  } catch (error) {
    console.error('获取用户信息时出错:', error)
    
    // 错误恢复：从localStorage读取角色，确保用户体验
    const storedRole = localStorage.getItem('role')
    if (storedRole && userStore.role !== parseInt(storedRole)) {
      // console.log('从localStorage恢复角色:', storedRole)
      userStore.role = parseInt(storedRole)
    }
  }
}

onMounted(() => {
  // console.log('Layout组件已挂载')
  
  // 从localStorage初始化角色，防止API调用失败
  const storedRole = localStorage.getItem('role')
  if (storedRole && userStore.role !== parseInt(storedRole)) {
    console.log('初始化时从localStorage设置角色:', storedRole)
    userStore.role = parseInt(storedRole)
  }
  
  // 立即刷新用户角色
  refreshUserRole().catch(err => {
    console.error('刷新用户角色失败:', err)
  })
  
  // 延迟再次检查角色，确保所有异步操作完成
  setTimeout(() => {
    // console.log('延迟检查角色:', userStore.role, '从localStorage:', localStorage.getItem('role'))
    
    // 如果角色仍不一致，尝试再次同步
    if (userStore.role !== parseInt(localStorage.getItem('role') || '0')) {
      console.log('角色不一致，进行同步')
      userStore.role = parseInt(localStorage.getItem('role') || '0')
    }
  }, 1000)
})

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