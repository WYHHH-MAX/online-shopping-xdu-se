import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginRequest, LoginResponse, RegisterRequest, ApiResponse } from '../types/auth'
import { login as loginApi, register as registerApi } from '../api/auth'
import { message } from 'ant-design-vue'
import type { AxiosResponse } from 'axios'

interface UserState {
  userId: number | null
  username: string
  nickname: string
  token: string
  role: number | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    userId: null,
    username: '',
    nickname: '',
    token: '',
    role: null
  }),
  
  actions: {
    setUserInfo(userInfo: LoginResponse) {
      this.userId = userInfo.userId
      this.username = userInfo.username
      this.nickname = userInfo.nickname
      this.token = userInfo.token
      this.role = userInfo.role
    },
    
    clearUserInfo() {
      this.userId = null
      this.username = ''
      this.nickname = ''
      this.token = ''
      this.role = null
    },

    async login(data: LoginRequest) {
      try {
        const res = await loginApi(data)
        const responseData = res.data as unknown as ApiResponse<LoginResponse>
        if (responseData && responseData.code === 200) {
          this.setUserInfo(responseData.data)
          return responseData
        } else {
          message.error(responseData?.message || '登录失败')
          return null
        }
      } catch (error: any) {
        message.error(error.response?.data?.message || error.message || '登录失败')
        return null
      }
    },

    async register(data: RegisterRequest) {
      try {
        const res = await registerApi(data)
        const responseData = res.data as unknown as ApiResponse<LoginResponse>
        if (responseData && responseData.code === 200) {
          this.setUserInfo(responseData.data)
          return responseData
        } else {
          message.error(responseData?.message || '注册失败')
          return null
        }
      } catch (error: any) {
        message.error(error.response?.data?.message || error.message || '注册失败')
        return null
      }
    }
  }
}) 