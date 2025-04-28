<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="header">
      <div class="logo">
        <img src="@/assets/logo.png" alt="Logo" height="40" />
        <span style="color: white; margin-left: 10px; font-size: 16px">Admin Portal</span>
      </div>
      <div class="user-info">
        <a-dropdown>
          <a class="user-dropdown" @click.prevent>
            <a-avatar :src="userStore.avatar || '@/assets/avatar.png'" />
            <span style="margin-left: 8px">{{ userStore.nickname }}</span>
            <down-outlined />
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item key="0" @click="logout">
                <logout-outlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    <a-layout>
      <a-layout-sider width="200" style="background: #fff">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          mode="inline"
          :style="{ height: '100%', borderRight: 0 }"
        >
          <a-menu-item v-for="item in menuItems" :key="item.key" @click="navigateTo(item.path)">
            <component :is="item.icon" />
            <span>{{ item.title }}</span>
          </a-menu-item>
        </a-menu>
      </a-layout-sider>
      <a-layout-content :style="{ padding: '0 24px', minHeight: '280px' }">
        <router-view></router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script lang="ts" setup>
import { ref, watch, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  ShopOutlined,
  UserOutlined,
  LogoutOutlined,
  DownOutlined,
  SolutionOutlined,
  ShoppingOutlined,
  ShoppingCartOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKeys = ref<string[]>(['dashboard'])
const openKeys = ref<string[]>([])

// 定义菜单项
const menuItems = reactive([
  { 
    key: 'dashboard', 
    path: '/admin', 
    title: '控制台', 
    icon: DashboardOutlined 
  },
  { 
    key: 'seller-requests', 
    path: '/admin/seller-requests', 
    title: 'Seller application',
    icon: SolutionOutlined 
  },
  { 
    key: 'users', 
    path: '/admin/users', 
    title: 'User management',
    icon: UserOutlined 
  },
  { 
    key: 'products', 
    path: '/admin/products', 
    title: 'Merchandise management',
    icon: ShoppingOutlined 
  },
  { 
    key: 'orders', 
    path: '/admin/orders', 
    title: 'Order management',
    icon: ShoppingCartOutlined 
  }
])

// 导航到指定路径
const navigateTo = (path: string) => {
  // console.log('点击菜单，导航到路径:', path)
  router.push(path)
}

// 根据当前路由设置选中的菜单项
watch(
  () => route.path,
  (path) => {
    // console.log('当前路由路径:', path);
    const pathParts = path.split('/');
    // console.log('路由部分:', pathParts);
    
    if (pathParts.length >= 3) {
      const menuKey = pathParts[2] || 'dashboard';
      // console.log('设置选中菜单项:', menuKey);
      selectedKeys.value = [menuKey];
      
      // 确保菜单项存在，否则默认选中dashboard
      const validMenuKeys = ['dashboard', 'seller-requests', 'users', 'products', 'orders'];
      if (!validMenuKeys.includes(menuKey)) {
        // console.warn('无效的菜单键:', menuKey, '将使用默认值dashboard');
        selectedKeys.value = ['dashboard'];
      }
    } else {
      console.log('使用默认菜单项: dashboard');
      selectedKeys.value = ['dashboard'];
    }
  },
  { immediate: true }
)

// 退出登录
const logout = () => {
  // 修复方法：使用类型断言确保调用正确
  (userStore as any).clearUserInfo?.() || userStore.$reset?.()
  message.success('Logged out')
  router.push('/login')
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
}

.user-info {
  color: white;
}

.user-dropdown {
  color: white;
  display: flex;
  align-items: center;
}
</style> 