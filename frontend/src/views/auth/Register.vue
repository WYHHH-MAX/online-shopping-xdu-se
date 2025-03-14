<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册</h2>
      <a-form
        :model="formState"
        name="register"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 16 }"
        autocomplete="off"
        @finish="onFinish"
      >
        <a-form-item
          label="用户名"
          name="username"
          :rules="[{ required: true, message: '请输入用户名!' }]"
        >
          <a-input v-model:value="formState.username" />
        </a-form-item>

        <a-form-item
          label="密码"
          name="password"
          :rules="[{ required: true, message: '请输入密码!' }]"
        >
          <a-input-password v-model:value="formState.password" />
        </a-form-item>

        <a-form-item
          label="昵称"
          name="nickname"
          :rules="[{ required: true, message: '请输入昵称!' }]"
        >
          <a-input v-model:value="formState.nickname" />
        </a-form-item>

        <a-form-item
          label="手机号"
          name="phone"
          :rules="[{ required: true, message: '请输入手机号!' }]"
        >
          <a-input v-model:value="formState.phone" />
        </a-form-item>

        <a-form-item
          label="邮箱"
          name="email"
          :rules="[{ required: true, message: '请输入邮箱!' }]"
        >
          <a-input v-model:value="formState.email" />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
          <a-button type="primary" html-type="submit">注册</a-button>
          <a-button style="margin-left: 10px" @click="goToLogin">返回登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

interface FormState {
  username: string
  password: string
  nickname: string
  phone: string
  email: string
}

const formState = reactive<FormState>({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: ''
})

const onFinish = async (values: FormState) => {
  try {
    console.log('发送注册请求:', values)
    const res = await register(values)
    console.log('注册响应:', res)
    if (res.code === 200) {
      message.success('注册成功')
      userStore.setUserInfo(res.data)
      router.push('/')
    } else {
      message.error(res.message || '注册失败')
    }
  } catch (error: any) {
    console.error('注册错误:', error)
    message.error(error.response?.data?.message || error.message || '注册失败')
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.register-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: rgba(0, 0, 0, 0.85);
}
</style> 