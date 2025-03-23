/**
 * 分页结果类型
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
  list?: T[]
} 