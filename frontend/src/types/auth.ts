export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
  phone: string
  email: string
}

export interface LoginResponse {
  userId: number
  username: string
  nickname: string
  token: string
  role: number
  avatar?: string
  phone?: string
  email?: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
} 