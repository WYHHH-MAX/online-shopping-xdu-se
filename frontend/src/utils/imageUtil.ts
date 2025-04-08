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

// 默认图片路径
const DEFAULT_IMAGE = `${BASE_IMAGE_URL}${API_PREFIX}/product/placeholder`;

/**
 * 处理图片URL，确保图片可以正确加载
 * @param url 原始图片URL
 * @returns 处理后的图片URL
 */
export function getImageUrl(url: string | null | undefined): string {
  // 处理null或undefined
  if (!url) {
    console.log('图片URL为空，返回默认图片');
    // 如果URL为空，返回默认图片
    return DEFAULT_IMAGE;
  }
  
  // 处理对象类型的URL (容错处理)
  if (typeof url === 'object') {
    console.log('接收到对象类型的URL:', url);
    
    // 尝试从常见属性中获取URL
    if (url && 'data' in url && typeof (url as any).data === 'string') {
      console.log('从对象的data属性提取URL:', (url as any).data);
      // 递归调用自身处理提取的URL
      return getImageUrl((url as any).data);
    }
    
    // 如果无法提取有效URL，返回默认图片
    console.warn('无法从对象中提取有效的URL:', url);
    return DEFAULT_IMAGE;
  }

  // 如果URL已经是完整的URL（以http或https开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // 处理URL路径
  const timestamp = new Date().getTime();
  
  // 处理重复的/api前缀
  if (url.startsWith('/api/api/')) {
    url = url.replace('/api/api/', '/api/');
  }
  
  // 确保所有URL都有正确的前缀
  if (url.startsWith('/api/')) {
    // URL已经有/api前缀，直接使用
    return `${BASE_IMAGE_URL}${url}?t=${timestamp}`;
  } else if (url.startsWith('/images/')) {
    // URL是/images/开头，添加/api前缀
    return `${BASE_IMAGE_URL}${API_PREFIX}${url}?t=${timestamp}`;
  } else if (url.startsWith('/')) {
    // 其他以/开头的路径，添加/api前缀
    return `${BASE_IMAGE_URL}${API_PREFIX}${url}?t=${timestamp}`;
  } else {
    // 没有/开头的相对路径，假设是相对于/images/目录
    return `${BASE_IMAGE_URL}${API_PREFIX}/images/${url}?t=${timestamp}`;
  }
}

/**
 * 处理图片URL数组
 * @param urls 图片URL数组
 * @returns 处理后的图片URL数组
 */
export function getImageUrls(urls: string[] | null | undefined): string[] {
  if (!urls || !Array.isArray(urls)) {
    return [];
  }
  return urls.map(url => getImageUrl(url));
}

/**
 * 处理图片加载错误，替换为默认图片
 * @param event 图片加载错误事件
 */
export function handleImageError(event: Event): void {
  const imgElement = event.target as HTMLImageElement;
  if (imgElement && imgElement.src !== DEFAULT_IMAGE) {
    console.warn('图片加载失败，替换为默认图片:', imgElement.src);
    imgElement.src = DEFAULT_IMAGE;
  }
} 