import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { mockAdminAPI } from '@/utils/mockData'

// 创建axios实例
const instance: AxiosInstance = axios.create({
  // 设置baseURL为/api，与后端context-path匹配
  baseURL: '/api',  
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 保持启用凭证
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    // 从store获取token
    const token = useUserStore().token
    // 如果有token，则添加到请求头中
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 开发环境下可能使用模拟数据 - 现在已禁用模拟数据功能
    if (import.meta.env.DEV) {
      // 模拟数据功能已禁用
      console.log('发送真实请求:', config.url)
    }
    
    return config
  },
  (error) => {
    console.error('请求错误', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    // 从响应中获取数据
    const res = response.data
    
    // 如果响应包含code字段且不是200或1，则抛出错误
    if (res && res.code !== undefined && res.code !== 200 && res.code !== 1) {
      message.error(res.message || res.msg || '请求失败')
      
      // 401: 未授权 - 重新登录
      if (res.code === 401) {
        // 弹出确认对话框
        message.error('您的登录状态已过期，请重新登录')
        
        // 清除用户信息
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('nickname')
        localStorage.removeItem('role')
        localStorage.removeItem('avatar')
        localStorage.removeItem('phone')
        localStorage.removeItem('email')
        
        // 重定向到登录页面
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || res.msg || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    // 确保不使用模拟数据
    console.error('响应错误', error)
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      if (status === 401) {
        // 清除用户信息
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('nickname')
        localStorage.removeItem('role')
        localStorage.removeItem('avatar')
        localStorage.removeItem('phone')
        localStorage.removeItem('email')
        
        // 重定向到登录页面
        window.location.href = '/login'
        message.error('登录状态已过期，请重新登录')
      } else if (status === 404) {
        message.error('请求的资源不存在')
      } else {
        message.error(error.response.data?.message || error.response.data?.msg || '请求失败')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      message.error('无法连接到服务器，请检查您的网络连接')
    } else {
      // 请求配置出错
      message.error(error.message || '请求过程中发生错误')
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法
export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return instance(config)
}

export default request 