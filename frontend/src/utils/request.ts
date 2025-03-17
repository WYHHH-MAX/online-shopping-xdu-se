import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

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
    // 添加调试日志 - 增强版本
    const fullURL = config.baseURL ? `${config.baseURL}${config.url}` : config.url;
    console.log(`[请求] ${config.method?.toUpperCase()} ${config.url}`, {
      fullURL: fullURL,
      data: config.data,
      params: config.params,
      headers: config.headers,
      mode: import.meta.env.MODE // 输出当前环境
    });
    
    // 获取token
    const token = localStorage.getItem('token');
    
    // 如果有token则添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('请求已附加token:', token.substring(0, 10) + '...');
    }
    
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    // 添加调试日志
    console.log(`[响应成功] ${response.config.method?.toUpperCase()} ${response.config.url}`, {
      status: response.status,
      data: response.data,
      headers: response.headers,
      config: {
        baseURL: response.config.baseURL,
        url: response.config.url,
        method: response.config.method
      }
    });
    
    const res = response.data;
    // 检查响应格式
    if (res.code === 200) {
      // 标准响应格式 {code: 200, data: xxx}
      console.log('标准响应格式，返回data字段');
      return res.data;
    } else if (res.code === 401) {
      // 未授权（token过期或无效）
      console.error('用户未认证或token已过期');
      message.error('登录已过期，请重新登录');
      
      // 清除token
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('nickname');
      localStorage.removeItem('role');
      
      // 重定向到登录页
      window.location.href = '/login';
      
      return Promise.reject(new Error('未授权，请重新登录'));
    } else if (res.total !== undefined && res.list !== undefined) {
      // 直接返回分页数据格式 {total: x, list: [...]}
      console.log('分页数据格式，转换为标准分页对象');
      return {
        records: res.list,
        total: res.total,
        size: res.list.length,
        current: 1,
        pages: Math.ceil(res.total / res.list.length) || 1
      };
    } else {
      // 直接返回数据
      console.log('其他响应格式，直接返回');
      return res;
    }
  },
  (error) => {
    // 增强的错误日志
    console.error('[请求错误]', {
      url: error.config?.url,
      method: error.config?.method,
      fullURL: (error.config?.baseURL || '') + (error.config?.url || ''),
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      message: error.message,
      request: {
        data: error.config?.data,
        params: error.config?.params,
        headers: error.config?.headers
      }
    });
    
    // 如果是404错误，可能是API路径问题
    if (error.response && error.response.status === 404) {
      console.error('API路径不存在，请检查请求URL是否正确:', (error.config?.baseURL || '') + (error.config?.url || ''));
      message.error(`请求的API路径不存在: ${error.config?.url}，请检查API配置`);
      return Promise.reject(new Error(`API路径不存在: ${error.config?.url}`));
    }
    
    // 如果是401错误，清除token并重定向到登录页
    if (error.response && error.response.status === 401) {
      console.error('用户未认证或token已过期');
      message.error('登录已过期，请重新登录');
      
      // 清除token
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('nickname');
      localStorage.removeItem('role');
      
      // 重定向到登录页
      window.location.href = '/login';
      return Promise.reject(new Error('未授权，请重新登录'));
    }
    
    // 提示错误信息
    if (error.response && error.response.data && error.response.data.message) {
      message.error(error.response.data.message);
    } else {
      message.error(error.message || '请求失败');
    }
    
    return Promise.reject(error);
  }
);

// 封装请求方法
export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return instance(config)
}

export default request 