export interface OrderVO {
  id: number
  orderNo: string
  status: string
  totalAmount: number
  createTime: string
  updateTime: string
  products: OrderProductVO[]
}

export interface OrderProductVO {
  id: number
  name: string
  image: string
  price: number
  quantity: number
} 