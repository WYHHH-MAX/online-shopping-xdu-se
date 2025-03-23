export interface CategoryVO {
  id: number
  parentId: number
  name: string
  level: number
  sort: number
  icon?: string
  createdTime?: string
  updatedTime?: string
  deleted?: number
  children?: CategoryVO[]
  parent?: CategoryVO
} 