import request from '@/utils/request'

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
  return request({
    url: '/users/current',
    method: 'get'
  })
} 