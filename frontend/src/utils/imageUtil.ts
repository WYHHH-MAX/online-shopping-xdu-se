/**
 * 图片URL处理工具
 */

// 图片服务器基础路径 - 根据当前环境自动选择
const BASE_IMAGE_URL = import.meta.env.PROD 
  ? window.location.origin  // 生产环境使用当前站点的源
  : 'http://localhost:8080'; // 开发环境使用固定地址

console.log('图片服务器基础路径:', BASE_IMAGE_URL);

// API路径前缀
const API_PREFIX = '/api';

/**
 * 处理图片URL，确保图片可以正确加载
 * @param url 原始图片URL
 * @returns 处理后的图片URL
 */
export function getImageUrl(url: string): string {
  if (!url) {
    // 如果URL为空，返回默认图片
    return `${BASE_IMAGE_URL}${API_PREFIX}/images/placeholder.jpg`;
  }

  // 如果URL已经是完整的URL（以http或https开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }

  // 始终添加时间戳，确保不使用缓存
  const timestamp = new Date().getTime();
  
  // 处理商品图片路径
  if (url.startsWith('/images/products/')) {
    return `${BASE_IMAGE_URL}${API_PREFIX}${url}?t=${timestamp}`;
  }

  // 处理特定的文件夹路径，比如头像路径
  if (url.startsWith('/uploads/') || url.startsWith('/api/uploads/')) {
    // 确保URL格式正确
    const apiUrl = url.startsWith('/api') ? url : `/api${url}`;
    return `${BASE_IMAGE_URL}${apiUrl}?t=${timestamp}`;
  }
  
  // 处理新的头像路径格式 (/images/avatars/...)
  if (url.startsWith('/images/avatars/')) {
    // 移除旧的时间戳（如果有）
    const cleanUrl = url.includes('?') ? url.split('?')[0] : url;
    return `${BASE_IMAGE_URL}${API_PREFIX}${cleanUrl}?t=${timestamp}`;
  }

  // 如果是相对路径，添加基础路径
  if (url.startsWith('/')) {
    return `${BASE_IMAGE_URL}${API_PREFIX}${url}?t=${timestamp}`;
  }

  // 其他情况，假设是相对于images目录的路径
  return `${BASE_IMAGE_URL}${API_PREFIX}/images/${url}?t=${timestamp}`;
}

/**
 * 处理图片URL数组
 * @param urls 图片URL数组
 * @returns 处理后的图片URL数组
 */
export function getImageUrls(urls: string[]): string[] {
  if (!urls || !Array.isArray(urls)) {
    return [];
  }
  return urls.map(url => getImageUrl(url));
} 