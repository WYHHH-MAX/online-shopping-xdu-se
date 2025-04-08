<template>
  <div class="register-container">
    <div class="register-box">
      <h2>register</h2>
      <a-form
        :model="formState"
        name="register"
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

        <a-form-item
          label="nickname"
          name="nickname"
          :rules="[{ required: true, message: 'Please enter your nickname!' }]"
        >
          <a-input v-model:value="formState.nickname" />
        </a-form-item>

        <a-form-item
          label="phone"
          name="phone"
          :rules="[{ required: true, message: 'Please enter your phone!' }]"
        >
          <a-input v-model:value="formState.phone" />
        </a-form-item>

        <a-form-item
          label="email"
          name="email"
          :rules="[{ required: true, message: 'Please enter your email!' }]"
        >
          <a-input v-model:value="formState.email" />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
          <a-button type="primary" html-type="submit">register</a-button>
          <a-button style="margin-left: 10px" @click="goToLogin">login</a-button>
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
import type { RegisterRequest } from '@/types/auth'

const router = useRouter()
const userStore = useUserStore()

const formState = reactive<RegisterRequest>({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: ''
})

const onFinish = async (values: RegisterRequest) => {
  try {
    const res = await register(values)
    if (res.code === 200 || res.code === 1) {
      message.success('注册成功')
      const loginResult = await userStore.login({
        username: values.username,
        password: values.password
      })
      if (loginResult) {
        router.push('/')
      }
    } else {
      message.error(res.message || '注册失败')
    }
  } catch (error: any) {
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