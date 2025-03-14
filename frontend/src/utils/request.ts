import axios from 'axios'
import type { AxiosInstance } from 'axios'
import { message } from 'ant-design-vue'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',  // 与 Vite 代理配置保持一致
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('响应数据:', response)
    return response
  },
  error => {
    console.error('请求错误:', {
      config: error.config,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    })
    message.error(error.response?.data?.message || error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request 