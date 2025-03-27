import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ApiResponse } from '@/types/auth'

export interface UserProfileData {
  nickname?: string
  phone?: string
  email?: string
  avatar?: string
}

/**
 * 更新用户资料
 * @param data 用户资料数据
 * @returns 响应结果
 */
export const updateUserProfile = (data: UserProfileData) => {
  console.log('调用更新用户资料API, 参数:', data)
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

/**
 * 上传用户头像
 * @param file 头像文件
 * @returns 头像URL
 */
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  console.log('调用上传头像API, 文件:', file.name, file.size)
  return request<string>({
    url: '/users/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取当前用户信息
 * @returns 用户信息
 */
export const getCurrentUser = () => {
  console.log('调用获取当前用户API')
  return request({
    url: '/users/current',
    method: 'get'
  }).catch(error => {
    console.error('获取当前用户API失败:', error)
    return null
  })
}

/**
 * 获取当前用户信息并更新store
 * 用于确保用户信息同步最新
 */
export const refreshCurrentUser = async () => {
  try {
    console.log('刷新当前用户信息')
    const response = await getCurrentUser() as ApiResponse<any>
    console.log('获取到用户信息响应:', response)
    
    if (!response) {
      console.error('响应为空')
      return null
    }
    
    if (response.code !== 1 && response.code !== 200) {
      console.error('响应code不正确:', response.code)
      return null
    }
    
    if (!response.data) {
      console.error('响应data为空')
      return null
    }
    
    // 导入store并更新用户信息
    const userStore = useUserStore()
    
    // 打印详细的响应数据结构
    console.log('用户数据结构:', Object.keys(response.data))
    
    // 构建LoginResponse格式对象，安全地访问属性
    const userData = {
      userId: response.data.id || 0,
      username: response.data.username || '',
      nickname: response.data.nickname || '',
      token: userStore.token || '', // 保留现有token
      role: typeof response.data.role === 'number' ? response.data.role : 0,
      avatar: response.data.avatar || '',
      phone: response.data.phone || '',
      email: response.data.email || ''
    }
    
    console.log('准备更新用户信息，角色:', userData.role)
    // 使用类型断言避免类型错误
    try {
      (userStore as any).setUserInfo(userData);
    } catch (e) {
      // 回退方案：使用$patch
      userStore.$patch(userData);
    }
    return userData
  } catch (error) {
    console.error('刷新用户信息失败，详细错误:', error)
    return null
  }
} 