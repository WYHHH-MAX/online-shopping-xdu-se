<template>
  <div class="profile">
    <a-row :gutter="24">
      <!-- 左侧用户信息 -->
      <a-col :span="8">
        <a-card>
          <template #cover>
            <img
              :src="userInfo.avatar || 'https://via.placeholder.com/200'"
              alt="avatar"
              class="avatar"
            />
          </template>
          <a-card-meta :title="userInfo.nickname">
            <template #description>
              <div>用户名：{{ userInfo.username }}</div>
              <div>角色：{{ userInfo.role === 0 ? '买家' : '卖家' }}</div>
              <div>手机：{{ userInfo.phone }}</div>
              <div>邮箱：{{ userInfo.email }}</div>
            </template>
          </a-card-meta>
          <a-button type="primary" block style="margin-top: 16px" @click="showEditModal">
            编辑资料
          </a-button>
        </a-card>
      </a-col>

      <!-- 右侧订单信息 -->
      <a-col :span="16">
        <a-card title="我的订单">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="all" tab="全部订单">
              <order-list :status="null" />
            </a-tab-pane>
            <a-tab-pane key="unpaid" tab="待付款">
              <order-list status="unpaid" />
            </a-tab-pane>
            <a-tab-pane key="paid" tab="已付款">
              <order-list status="paid" />
            </a-tab-pane>
            <a-tab-pane key="shipped" tab="已发货">
              <order-list status="shipped" />
            </a-tab-pane>
            <a-tab-pane key="completed" tab="已完成">
              <order-list status="completed" />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <!-- 编辑资料弹窗 -->
    <a-modal
      v-model:visible="editModalVisible"
      title="编辑资料"
      @ok="handleEditSubmit"
      :confirmLoading="editLoading"
    >
      <a-form :model="editForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="昵称" name="nickname">
          <a-input v-model:value="editForm.nickname" />
        </a-form-item>
        <a-form-item label="手机" name="phone">
          <a-input v-model:value="editForm.phone" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="editForm.email" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import OrderList from '@/components/OrderList.vue'

const userStore = useUserStore()
const userInfo = userStore.$state

const activeTab = ref('all')
const editModalVisible = ref(false)
const editLoading = ref(false)

const editForm = ref({
  nickname: userInfo.nickname,
  phone: userInfo.phone,
  email: userInfo.email
})

const showEditModal = () => {
  editModalVisible.value = true
  editForm.value = {
    nickname: userInfo.nickname,
    phone: userInfo.phone,
    email: userInfo.email
  }
}

const handleEditSubmit = async () => {
  try {
    editLoading.value = true
    // TODO: 调用更新用户信息的API
    message.success('更新成功')
    editModalVisible.value = false
  } catch (error) {
    message.error('更新失败')
  } finally {
    editLoading.value = false
  }
}
</script>

<style scoped>
.profile {
  padding: 20px;
}

.avatar {
  width: 200px;
  height: 200px;
  object-fit: cover;
  display: block;
  margin: 0 auto;
}
</style> 