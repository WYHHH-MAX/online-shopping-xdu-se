<template>
  <div class="profile">
    <a-row :gutter="24">
      <!-- 左侧用户信息 -->
      <a-col :span="8">
        <a-card>
          <template #cover>
            <div class="avatar-container">
              <img
                :src="avatarUrl"
                alt="avatar"
                class="avatar"
                :key="'avatar-' + forceRefresh"
              />
              <div class="avatar-overlay" @click="handleAvatarClick">
                <a-upload
                  :show-upload-list="false"
                  :before-upload="beforeAvatarUpload"
                  :customRequest="customAvatarUpload"
                >
                  <div class="upload-text">
                    <i class="anticon anticon-camera"></i>
                    <div>Change your avater</div>
                  </div>
                </a-upload>
              </div>
            </div>
          </template>
          <a-card-meta :title="displayName">
            <template #description>
              <div>username:{{ userInfo.username }}</div>
              <div>role:{{ userInfo.role === 0 ? 'buyer' : 'seller' }}</div>
              <div>phone:{{ displayPhone }}</div>
              <div>Email:{{ displayEmail }}</div>
            </template>
          </a-card-meta>
          <a-button type="primary" block style="margin-top: 16px" @click="showEditModal">
            Edit profile information
          </a-button>
        </a-card>
      </a-col>

      <!-- 右侧订单信息 -->
      <a-col :span="16">
        <a-card title="My Orders">
          <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
            <a-tab-pane key="all" tab="All orders">
              <order-list :status="''" />
            </a-tab-pane>
            <a-tab-pane key="0" tab="Pending payment">
              <order-list status="0" />
            </a-tab-pane>
            <a-tab-pane key="1" tab="To be shipped">
              <order-list status="1" />
            </a-tab-pane>
            <a-tab-pane key="2" tab="To be received">
              <order-list status="2" />
            </a-tab-pane>
            <a-tab-pane key="3" tab="Done">
              <order-list status="3" />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <!-- 编辑资料弹窗 -->
    <a-modal
      v-model:visible="editModalVisible"
      title="Edit your profile"
      @ok="handleEditSubmit"
      :confirmLoading="editLoading"
    >
      <a-form :model="editForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="nickname" name="nickname">
          <a-input v-model:value="editForm.nickname" />
        </a-form-item>
        <a-form-item label="phone" name="phone">
          <a-input v-model:value="editForm.phone" />
        </a-form-item>
        <a-form-item label="email" name="email">
          <a-input v-model:value="editForm.email" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { message, Modal, UploadProps } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import OrderList from '@/components/OrderList.vue'
import { updateUserProfile, uploadAvatar, getCurrentUser } from '../api/user'
import { getImageUrl } from '../utils/imageUtil'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const userInfo = computed(() => userStore)
const forceRefresh = ref(0) // 添加强制刷新计数器

const activeTab = ref('all')
const editModalVisible = ref(false)
const editLoading = ref(false)

const editForm = ref({
  nickname: userInfo.value.nickname || '',
  phone: userInfo.value.phone || '',
  email: userInfo.value.email || ''
})

// 头像默认值设置
const avatarUrl = computed(() => {
  // 使用forceRefresh作为依赖，当它变化时重新计算
  forceRefresh.value; // 仅用于触发依赖收集
  
  if (!userInfo.value.avatar) {
    // console.log('用户没有头像，使用默认头像');
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${userInfo.value.username}`;
  }

  // 确保avatar是字符串
  const avatar = String(userInfo.value.avatar);
  // console.log('用户头像路径:', avatar);

  // 使用工具函数处理图片URL - 新路径格式 /images/avatars/user_X.png
  const timestamp = new Date().getTime(); // 添加时间戳避免缓存
  const url = getImageUrl(avatar) + `?t=${timestamp}`;
  // console.log('处理后的头像URL:', url);
  
  return url;
})

// 显示默认值或占位符
const displayName = computed(() => userInfo.value.nickname || userInfo.value.username || '未设置')
const displayPhone = computed(() => userInfo.value.phone || '未设置')
const displayEmail = computed(() => userInfo.value.email || '未设置')

const router = useRouter()

// 从后端加载用户信息
const loadUserInfo = async () => {
  try {
    // 首先检查用户是否已登录
    if (!userStore.isLoggedIn()) {
      console.warn('用户未登录，重定向到登录页面')
      router.push('/login?redirect=/profile')
      return
    }
    
    // 验证当前token是否有效
    const isTokenValid = await userStore.validateToken()
    if (!isTokenValid) {
      console.warn('Token无效，重定向到登录页面')
      router.push('/login?redirect=/profile')
      return
    }
    
    const userData = await getCurrentUser() as any
    // console.log('从后端获取的用户信息:', userData)
    
    // 更新存储的用户信息
    if (userData && userData.data) {
      const user = userData.data
      // 更新用户信息
      userStore.$patch({
        userId: user.id,
        username: user.username,
        nickname: user.nickname || '',
        // 保留现有token，不从响应中更新
        token: userStore.token,
        role: user.role,
        authenticated: true,
        avatar: user.avatar || '',
        phone: user.phone || '',
        email: user.email || ''
      })
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 出错时也尝试重定向到登录页面
    router.push('/login?redirect=/profile')
  }
}

// 处理标签页切换
const handleTabChange = (key: string) => {
  // console.log('标签页切换:', key);
  activeTab.value = key;
}

const showEditModal = () => {
  editModalVisible.value = true
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    email: userInfo.value.email || ''
  }
}

const handleEditSubmit = async () => {
  try {
    editLoading.value = true
    
    // console.log('更新用户信息:', editForm.value)
    
    // 调用API更新用户信息
    await updateUserProfile({
      nickname: editForm.value.nickname,
      phone: editForm.value.phone,
      email: editForm.value.email
    })
    
    // 更新成功后重新获取用户信息
    await loadUserInfo()
    
    message.success('更新成功')
    editModalVisible.value = false
  } catch (error) {
    // console.error('更新失败:', error)
    message.error('更新失败')
  } finally {
    editLoading.value = false
  }
}

// 处理头像上传前的校验
const beforeAvatarUpload = (file: File) => {
  // 检查文件类型
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif';
  if (!isJpgOrPng) {
    message.error('只能上传JPG/PNG/GIF图片!');
    return false;
  }
  
  // 检查文件大小
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    message.error('图片必须小于5MB!');
    return false;
  }
  
  return true;
}

// 点击头像显示上传区域
const handleAvatarClick = () => {
  console.log('点击了头像');
}

// 自定义上传实现
const customAvatarUpload = async (options: any) => {
  const { file, onSuccess, onError } = options;
  
  try {
    // 上传头像
    const avatarPath = await uploadAvatar(file);
    // console.log('头像上传成功，返回路径:', avatarPath)
    
    // 上传成功后，更新用户信息
    await loadUserInfo()
    
    // 触发强制刷新
    forceRefresh.value += 1;
    // console.log('触发头像强制刷新, 计数:', forceRefresh.value);
    
    message.success('头像上传成功');
    onSuccess(avatarPath, file);
  } catch (error) {
    // console.error('头像上传失败:', error);
    message.error('头像上传失败');
    onError(error);
  }
}

// 初始化时加载用户信息
onMounted(() => {
  // console.log('用户信息:', userInfo.value);
  
  // 从后端获取最新用户信息
  loadUserInfo();
  
  // 检查URL参数，如果有tab参数，则切换到对应标签
  const urlParams = new URLSearchParams(window.location.search)
  const tab = urlParams.get('tab')
  if (tab) {
    // console.log('从URL参数中读取标签:', tab);
    activeTab.value = tab
  }
  
  // console.log('当前激活的标签页:', activeTab.value);
})
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

.avatar-container {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.upload-text {
  color: #fff;
  text-align: center;
  font-size: 16px;
}
</style> 