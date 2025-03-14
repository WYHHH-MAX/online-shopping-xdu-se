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
          path: 'cart',
          name: 'Cart',
          component: () => import('@/views/Cart.vue')
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
        },
        {
          path: 'checkout',
          name: 'Checkout',
          component: () => import('@/views/Checkout.vue')
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