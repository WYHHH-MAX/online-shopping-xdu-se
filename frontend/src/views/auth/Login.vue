<template>
  <div class="login-container">
    <div class="login-box">
      <h2>login</h2>
      <a-form
        :model="formState"
        name="basic"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 16 }"
        autocomplete="off"
        @finish="onFinish"
      >
        <a-form-item
          label="Username"
          name="username"
          :rules="[{ required: true, message: 'Please enter a username!' }]"
        >
          <a-input v-model:value="formState.username" />
        </a-form-item>

        <a-form-item
          label="Password"
          name="password"
          :rules="[{ required: true, message: 'Please enter your password!' }]"
        >
          <a-input-password v-model:value="formState.password" />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
          <a-button type="primary" html-type="submit">login</a-button>
          <a-button style="margin-left: 10px" @click="goToRegister">register</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import type { LoginResponse } from '@/types/auth'

const router = useRouter()
const userStore = useUserStore()

// 获取重定向URL（如果有）
const route = useRoute()
const redirectUrl = route.query.redirect as string || '/'

interface FormState {
  username: string
  password: string
}

const formState = reactive<FormState>({
  username: '',
  password: ''
})

const onFinish = async (values: FormState) => {
  try {
    // console.log('发送登录请求:', values)
    const res = await login(values.username, values.password)
    if (res) {
      // console.log('登录响应详情:', JSON.stringify(res))
      // console.log('登录用户角色:', res.role, '类型:', typeof res.role)
      
      // 修复类型错误：使用双重类型断言处理响应
      const userData = ('data' in res ? res.data : res) as any as LoginResponse;
      (userStore as any).setUserInfo(userData)
      
      // console.log('设置用户信息后存储里的角色:', userStore.role, '类型:', typeof userStore.role)
      // console.log('localStorage中的角色:', localStorage.getItem('role'))
      message.success('登录成功')
      
      // 根据用户角色跳转到不同页面
      const userRole = userData.role || 0
      if (userRole === 2) {
        // 管理员
        // console.log('检测到管理员角色，跳转到管理员页面')
        router.push('/admin')
      } else {
        // 优先使用重定向URL，如果有的话
        if (redirectUrl && redirectUrl !== '/login') {
          // console.log('检测到重定向URL，跳转到:', redirectUrl)
          router.push(redirectUrl)
        } else {
          // 没有重定向URL时才跳转到首页
          // console.log('没有重定向URL或重定向到登录页面，跳转到首页')
          router.push('/')
        }
      }
    }
  } catch (error: any) {
    // console.error('登录错误:', error)
    message.error(error.message || '登录失败，请检查用户名和密码')
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: rgba(0, 0, 0, 0.85);
}
</style> 