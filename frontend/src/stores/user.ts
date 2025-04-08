import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginRequest, LoginResponse, RegisterRequest, ApiResponse } from '../types/auth'
import { login as loginApi, register as registerApi, checkToken } from '../api/auth'
import { message } from 'ant-design-vue'
import type { AxiosResponse } from 'axios'

interface UserState {
  userId: number | null
  username: string
  nickname: string
  token: string
  role: number | null
  authenticated: boolean
  avatar: string
  phone: string
  email: string
}

export const useUserStore = defineStore('user', {
  state: (): UserState => {
    // 从localStorage恢复用户状态
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')
    const username = localStorage.getItem('username')
    const nickname = localStorage.getItem('nickname')
    const role = localStorage.getItem('role')
    const avatar = localStorage.getItem('avatar')
    const phone = localStorage.getItem('phone')
    const email = localStorage.getItem('email')
    
    // console.log('初始化用户store, 从localStorage读取角色:', role, '类型:', typeof role)
    
    // 确保role是数字类型
    let roleNumber: number | null = null
    if (role !== null) {
      try {
        roleNumber = parseInt(role)
        // console.log('解析角色为数字:', roleNumber)
      } catch (e) {
        console.error('角色解析失败:', e)
      }
    }
    
    return {
      userId: userId ? parseInt(userId) : null,
      username: username || '',
      nickname: nickname || '',
      token: token || '',
      role: roleNumber,
      authenticated: !!token,
      avatar: avatar || '',
      phone: phone || '',
      email: email || ''
    }
  },
  
  actions: {
    setUserInfo(userInfo: LoginResponse) {
      if (!userInfo) {
        console.error('用户信息为空，无法设置');
        return;
      }
      
      // console.log('设置用户信息, 角色:', userInfo.role, '类型:', typeof userInfo.role);
      
      this.userId = userInfo.userId || null;
      this.username = userInfo.username || '';
      this.nickname = userInfo.nickname || '';
      this.token = userInfo.token || '';
      this.role = userInfo.role || null;
      this.authenticated = !!userInfo.token;
      this.avatar = userInfo.avatar || '';
      this.phone = userInfo.phone || '';
      this.email = userInfo.email || '';
      
      // 保存到localStorage，强制类型转换确保一致性
      if (userInfo.token) localStorage.setItem('token', userInfo.token);
      if (userInfo.userId) localStorage.setItem('userId', String(userInfo.userId));
      if (userInfo.username) localStorage.setItem('username', userInfo.username);
      if (userInfo.nickname) localStorage.setItem('nickname', userInfo.nickname);
      if (userInfo.role !== undefined && userInfo.role !== null) {
        const roleStr = String(userInfo.role);
        localStorage.setItem('role', roleStr);
        // console.log('存储用户角色到localStorage:', roleStr);
      }
      if (userInfo.avatar) localStorage.setItem('avatar', userInfo.avatar);
      if (userInfo.phone) localStorage.setItem('phone', userInfo.phone);
      if (userInfo.email) localStorage.setItem('email', userInfo.email);
      
      // console.log('用户信息已保存，token:', userInfo.token, '角色:', this.role);
    },
    
    clearUserInfo() {
      this.userId = null
      this.username = ''
      this.nickname = ''
      this.token = ''
      this.role = null
      this.authenticated = false
      this.avatar = ''
      this.phone = ''
      this.email = ''
      
      // 清除localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('nickname')
      localStorage.removeItem('role')
      localStorage.removeItem('avatar')
      localStorage.removeItem('phone')
      localStorage.removeItem('email')
      
      // console.log('用户信息已清除')
    },

    async login(data: LoginRequest) {
      try {
        const { username, password } = data
        const response = await loginApi(username, password)
        // console.log('登录成功，响应:', response)
        this.setUserInfo(response)
        return response
      } catch (error: any) {
        // console.error('登录失败:', error)
        message.error(error.message || '登录失败')
        return null
      }
    },

    async register(data: RegisterRequest) {
      try {
        const response = await registerApi(data)
        // console.log('注册成功，响应:', response)
        if (response.code === 200 || response.code === 1) {
          if (response.data) {
            this.setUserInfo(response.data)
          }
          return response
        } else {
          message.error(response.message || '注册失败')
          return null
        }
      } catch (error: any) {
        // console.error('注册失败:', error)
        message.error(error.message || '注册失败')
        return null
      }
    },
    
    // 检查用户是否已登录
    isLoggedIn() {
      return this.authenticated && !!this.token && !!this.userId
    },
    
    // 检查并恢复用户会话
    checkAndRestoreSession() {
      const token = localStorage.getItem('token')
      if (!token) {
        this.clearUserInfo()
        return false
      }
      return true
    },
    
    // 验证当前token是否有效
    async validateToken() {
      try {
        if (!this.token) {
          return false
        }
        // 调用验证token的API
        const result = await checkToken()
        return result && result.code === 200
      } catch (error) {
        console.error('Token验证失败:', error)
        this.clearUserInfo()
        return false
      }
    }
  }
}) 