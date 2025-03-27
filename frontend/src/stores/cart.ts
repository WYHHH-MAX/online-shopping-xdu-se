import { defineStore } from 'pinia'

// 购物车状态接口
interface CartState {
  cartCount: number
}

// 定义购物车store
export const useCartStore = defineStore('cart', {
  state: (): CartState => ({
    cartCount: 0
  }),
  
  actions: {
    // 设置购物车数量
    setCartCount(count: number) {
      this.cartCount = count
      // console.log('购物车数量已更新:', count)
    },
    
    // 增加购物车数量
    incrementCartCount(amount: number = 1) {
      this.cartCount += amount
      // console.log('购物车数量已增加:', amount, '现在总数:', this.cartCount)
    }
  }
}) 