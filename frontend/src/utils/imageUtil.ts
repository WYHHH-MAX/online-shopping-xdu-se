/**
 * 图片URL处理工具
 */

// 图片服务器基础路径
const BASE_IMAGE_URL = 'http://localhost:8080';

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

  // 处理商品图片路径
  if (url.startsWith('/images/products/')) {
    // 添加API前缀和时间戳以防止缓存问题
    const timestamp = new Date().getTime();
    return `${BASE_IMAGE_URL}${API_PREFIX}${url}?t=${timestamp}`;
  }

  // 处理特定的文件夹路径，比如头像路径
  if (url.startsWith('/uploads/') || url.startsWith('/api/uploads/')) {
    // 确保URL格式正确
    const apiUrl = url.startsWith('/api') ? url : `/api${url}`;
    const formattedUrl = `${BASE_IMAGE_URL}${apiUrl}`;
    
    // 添加时间戳以防止缓存问题
    const timestamp = new Date().getTime();
    return `${formattedUrl}?t=${timestamp}`;
  }
  
  // 处理新的头像路径格式 (/images/avatars/...)
  if (url.startsWith('/images/avatars/')) {
    // 检查URL是否已包含时间戳，如果有则保留，否则添加新的
    if (url.includes('?t=')) {
      return `${BASE_IMAGE_URL}${API_PREFIX}${url}`;
    } else {
      const timestamp = new Date().getTime();
      return `${BASE_IMAGE_URL}${API_PREFIX}${url}?t=${timestamp}`;
    }
  }

  // 如果是相对路径，添加基础路径
  if (url.startsWith('/')) {
    return `${BASE_IMAGE_URL}${API_PREFIX}${url}`;
  }

  // 其他情况，假设是相对于images目录的路径
  return `${BASE_IMAGE_URL}${API_PREFIX}/images/${url}`;
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