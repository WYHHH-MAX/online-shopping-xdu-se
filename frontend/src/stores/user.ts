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
  authenticated: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => {
    // 从localStorage恢复用户状态
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')
    const username = localStorage.getItem('username')
    const nickname = localStorage.getItem('nickname')
    const role = localStorage.getItem('role')
    
    return {
      userId: userId ? parseInt(userId) : null,
      username: username || '',
      nickname: nickname || '',
      token: token || '',
      role: role ? parseInt(role) : null,
      authenticated: !!token
    }
  },
  
  actions: {
    setUserInfo(userInfo: LoginResponse) {
      this.userId = userInfo.userId
      this.username = userInfo.username
      this.nickname = userInfo.nickname
      this.token = userInfo.token
      this.role = userInfo.role
      this.authenticated = true
      
      // 保存到localStorage
      localStorage.setItem('token', userInfo.token)
      localStorage.setItem('userId', userInfo.userId.toString())
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('nickname', userInfo.nickname || '')
      localStorage.setItem('role', userInfo.role.toString())
      
      console.log('用户信息已保存，token:', userInfo.token)
    },
    
    clearUserInfo() {
      this.userId = null
      this.username = ''
      this.nickname = ''
      this.token = ''
      this.role = null
      this.authenticated = false
      
      // 清除localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('nickname')
      localStorage.removeItem('role')
      
      console.log('用户信息已清除')
    },

    async login(data: LoginRequest) {
      try {
        const { username, password } = data
        const response = await loginApi(username, password)
        console.log('登录成功，响应:', response)
        this.setUserInfo(response)
        return response
      } catch (error: any) {
        console.error('登录失败:', error)
        message.error(error.message || '登录失败')
        return null
      }
    },

    async register(data: RegisterRequest) {
      try {
        const { username, password, nickname } = data
        const response = await registerApi(username, password, nickname)
        console.log('注册成功，响应:', response)
        this.setUserInfo(response)
        return response
      } catch (error: any) {
        console.error('注册失败:', error)
        message.error(error.message || '注册失败')
        return null
      }
    },
    
    // 检查用户是否已登录
    isLoggedIn() {
      return this.authenticated && !!this.token
    }
  }
}) 