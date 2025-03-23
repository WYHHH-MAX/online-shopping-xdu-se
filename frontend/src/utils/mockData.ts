import { AxiosRequestConfig } from 'axios';

/**
 * 模拟数据工具，可以拦截前端请求并返回模拟数据
 * 注意：该功能已被禁用，所有请求将发送到真实后端
 */

/**
 * 模拟管理员API
 * @param {string} url API路径
 * @param {any} params 请求参数
 * @returns {Promise<any>} 模拟响应
 */
export function mockAdminAPI(url: string, params: any = {}): Promise<any> {
  console.log('模拟数据功能已禁用，请求将发送到后端:', url, params);
  return Promise.reject(new Error('模拟数据功能已禁用'));
} 