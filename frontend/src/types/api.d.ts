declare module '@/api/product' {
  import type { ProductVO, SearchProductsParams, PageResult } from '@/types/product'
  
  export function searchProducts(params: SearchProductsParams): Promise<{
    records: ProductVO[]
    total: number
    size: number
    current: number
    pages: number
  }>
  
  export function getProductDetail(id: number): Promise<ProductVO>
  
  export function getFeaturedProducts(page?: number, size?: number): Promise<PageResult<ProductVO>>
  
  export function getProductsByCategory(
    categoryId: number,
    page: number,
    size: number,
    sortBy?: string
  ): Promise<{
    records: ProductVO[]
    total: number
  }>

  export function getNewProducts(page?: number, size?: number): Promise<PageResult<ProductVO>>
  
  export function getHotProducts(page?: number, size?: number): Promise<PageResult<ProductVO>>
}

declare module '@/api/category' {
  import type { CategoryVO } from '@/types/category'
  
  export function getCategoryTree(): Promise<CategoryVO[]>
  
  export function getCategories(parentId?: number): Promise<CategoryVO[]>
  
  export function getCategoriesByLevel(level: number): Promise<CategoryVO[]>
  
  export function getCategoriesByParentId(parentId: number): Promise<CategoryVO[]>
  
  export function createCategory(data: CategoryVO): Promise<void>
  
  export function updateCategory(data: CategoryVO): Promise<void>
  
  export function deleteCategory(id: number): Promise<void>
  
  export function getCategoryById(id: number): Promise<CategoryVO>
} 