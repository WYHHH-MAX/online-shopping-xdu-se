import request from '@/utils/request'
import type { LoginResponse, ApiResponse } from '@/types/auth'

// 登录
export function login(username: string, password: string): Promise<LoginResponse> {
  return request<ApiResponse<LoginResponse>>({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
  }).then(res => res.data);
}

// 注册
export function register(username: string, password: string, nickname: string): Promise<LoginResponse> {
  return request<ApiResponse<LoginResponse>>({
    url: '/auth/register',
    method: 'post',
    data: { username, password, nickname }
  }).then(res => res.data);
} 
