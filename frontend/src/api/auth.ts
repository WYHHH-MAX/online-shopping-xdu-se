import request from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, ApiResponse } from '@/types/auth'

// 登录
export function login(username: string, password: string) {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
  })
}

// 注册
export function register(username: string, password: string, nickname: string) {
  return request<LoginResponse>({
    url: '/auth/register',
    method: 'post',
    data: { username, password, nickname }
  })
} 
