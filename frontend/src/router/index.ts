import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/components/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/Home.vue')
        },
        {
          path: 'category/:id',
          name: 'Category',
          component: () => import('@/views/Category.vue'),
          props: true
        },
        {
          path: 'cart',
          name: 'Cart',
          component: () => import('@/views/cart/CartPage.vue')
        },
        {
          path: 'checkout',
          name: 'Checkout',
          component: () => import('@/views/cart/Checkout.vue')
        },
        {
          path: 'payment/:orderNo',
          name: 'Payment',
          component: () => import('@/views/cart/Payment.vue'),
          props: true
        },
        {
          path: 'payment-success',
          name: 'PaymentSuccess',
          component: () => import('@/views/cart/PaymentSuccess.vue')
        },
        {
          path: 'orders',
          name: 'OrderList',
          component: () => import('@/views/order/OrderList.vue')
        },
        {
          path: 'order/:id',
          name: 'OrderDetail',
          component: () => import('@/views/order/OrderDetail.vue'),
          props: true
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue')
        },
        {
          path: 'product/:id',
          name: 'ProductDetail',
          component: () => import('@/views/ProductDetail.vue'),
          props: true
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router 