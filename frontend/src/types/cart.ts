/**
 * 购物车项VO
 */
export interface CartItemVO {
  id: number
  userId: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  selected: boolean
  stock: number
  createTime: string
  updateTime: string
}

/**
 * 购物车结算DTO
 */
export interface CartCheckoutDTO {
  cartItemIds: number[]
} 