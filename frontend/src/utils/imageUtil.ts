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
    // 但如果是无效的示例域名，替换为本地路径
    if (url.includes('example.com')) {
      // 从URL中提取文件名
      const fileName = url.substring(url.lastIndexOf('/') + 1);
      return `${BASE_IMAGE_URL}${API_PREFIX}/images/products/${fileName}`;
    }
    return url;
  }

  // 如果是相对路径，添加基础路径和API前缀
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