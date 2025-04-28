<template>
  <div class="seller-layout">
    <a-layout style="min-height: 100vh">
      <a-layout-sider v-model:collapsed="collapsed" collapsible>
        <div class="logo">
          <h2 v-if="!collapsed">Seller Central</h2>
          <h2 v-else>sales</h2>
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          theme="dark"
          mode="inline"
        >
          <a-menu-item key="dashboard">
            <router-link to="/seller">
              <dashboard-outlined />
              <span>Seller dashboard</span>
            </router-link>
          </a-menu-item>
          <a-menu-item key="products">
            <router-link to="/seller/products">
              <shopping-outlined />
              <span>Merchandise management</span>
            </router-link>
          </a-menu-item>
          <a-menu-item key="orders">
            <router-link to="/seller/orders">
              <shopping-cart-outlined />
              <span>Order management</span>
            </router-link>
          </a-menu-item>
          <a-menu-item key="inventory">
            <router-link to="/seller/inventory">
              <database-outlined />
              <span>Inventory management</span>
            </router-link>
          </a-menu-item>
          <a-menu-item key="sales">
            <router-link to="/seller/sales">
              <pie-chart-outlined />
              <span>Sales Data Analytics</span>
            </router-link>
          </a-menu-item>
          <a-menu-item key="profile">
            <router-link to="/seller/profile">
              <user-outlined />
              <span>Store information</span>
            </router-link>
          </a-menu-item>
          <a-menu-item key="payment">
            <router-link to="/seller/payment">
              <credit-card-outlined />
              <span>Payment Settings</span>
            </router-link>
          </a-menu-item>
          <a-menu-item key="home">
            <router-link to="/">
              <home-outlined />
              <span>Return to the Marketplace</span>
            </router-link>
          </a-menu-item>
        </a-menu>
      </a-layout-sider>
      <a-layout>
        <a-layout-header style="background: #fff; padding: 0">
          <div class="header-controls">
            <div class="header-left"></div>
            <div class="header-right">
              <a-dropdown>
                <a class="ant-dropdown-link" @click.prevent>
                  {{ userInfo.nickname || userInfo.username }} <down-outlined />
                </a>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="logout">
                      <logout-outlined />
                      <span>Sign out</span>
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </div>
        </a-layout-header>
        <a-layout-content style="margin: 0 16px">
          <div :style="{ padding: '24px', background: '#fff', minHeight: '360px' }">
            <router-view></router-view>
          </div>
        </a-layout-content>
        <a-layout-footer style="text-align: center">
          Online Marketplace Seller Central ©2023 Created by Your Company
        </a-layout-footer>
      </a-layout>
    </a-layout>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  ShoppingOutlined,
  ShoppingCartOutlined,
  DatabaseOutlined,
  UserOutlined,
  DownOutlined,
  HomeOutlined,
  LogoutOutlined,
  PieChartOutlined,
  CreditCardOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore)

// 根据当前路由设置选中的菜单项
const selectedKeys = ref<string[]>(['dashboard'])

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (path) => {
    const key = path.split('/').pop() || 'dashboard'
    selectedKeys.value = [key]
  },
  { immediate: true }
)

// 退出登录
const logout = () => {
  userStore.clearUserInfo()
  message.success('Logout successful')
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 32px;
  margin: 16px;
  color: white;
  text-align: center;
}

.header-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  width: 100%;
}

.header-left {
  width: 120px;  /* 与原按钮宽度相近 */
}

.header-right {
  display: flex;
  justify-content: flex-end;
}

.ant-dropdown-link {
  cursor: pointer;
  color: rgba(0, 0, 0, 0.65);
}
</style> 