import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/auth/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/auth/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('../components/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('../views/Home.vue')
        },
        {
          path: 'category/:id',
          name: 'Category',
          component: () => import('../views/Category.vue'),
          props: true
        },
        {
          path: 'cart',
          name: 'Cart',
          component: () => import('../views/cart/CartPage.vue')
        },
        {
          path: 'checkout',
          name: 'Checkout',
          component: () => import('../views/cart/Checkout.vue')
        },
        {
          path: 'payment/:orderNo',
          name: 'Payment',
          component: () => import('../views/cart/Payment.vue'),
          props: true
        },
        {
          path: 'payment-success/:orderNo',
          name: 'PaymentSuccess',
          component: () => import('../views/cart/PaymentSuccess.vue'),
          props: true
        },
        {
          path: 'orders',
          name: 'OrderList',
          component: () => import('../views/order/OrderList.vue')
        },
        {
          path: 'order/:id',
          name: 'OrderDetail',
          component: () => import('../views/order/OrderDetail.vue'),
          props: true
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/Profile.vue')
        },
        {
          path: 'product/:id',
          name: 'ProductDetail',
          component: () => import('../views/ProductDetail.vue'),
          props: true
        },
        {
          path: 'search',
          name: 'SearchResults',
          component: () => import('../views/SearchResults.vue')
        },
        {
          path: 'review',
          name: 'ProductReview',
          component: () => import('../views/review/ProductReview.vue')
        }
      ]
    },
    {
      path: '/seller',
      name: 'SellerLayout',
      component: () => import('../components/SellerLayout.vue'),
      meta: { requiresAuth: true, sellerOnly: true },
      children: [
        {
          path: '',
          name: 'SellerDashboard',
          component: () => import('../views/seller/Dashboard.vue')
        },
        {
          path: 'products',
          name: 'SellerProducts',
          component: () => import('../views/seller/Products.vue')
        },
        {
          path: 'products/add',
          name: 'AddProduct',
          component: () => import('../views/seller/ProductForm.vue')
        },
        {
          path: 'products/edit/:id',
          name: 'EditProduct',
          component: () => import('../views/seller/ProductForm.vue'),
          props: true
        },
        {
          path: 'orders',
          name: 'SellerOrders',
          component: () => import('../views/seller/Orders.vue')
        },
        {
          path: 'inventory',
          name: 'Inventory',
          component: () => import('../views/seller/Inventory.vue')
        },
        {
          path: 'profile',
          name: 'SellerProfile',
          component: () => import('../views/seller/Profile.vue')
        },
        {
          path: 'sales',
          name: 'SellerSales',
          component: () => import('../views/seller/SalesAnalytics.vue')
        },
        {
          path: 'payment',
          name: 'SellerPayment',
          component: () => import('../views/seller/Payment.vue')
        }
      ]
    },
    {
      path: '/apply-seller',
      name: 'ApplySeller',
      component: () => import('../views/seller/Apply.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/admin',
      name: 'AdminLayout',
      component: () => import('@/components/AdminLayout.vue'),
      meta: { 
        requiresAuth: true,
        roles: [2] // 只允许管理员访问
      },
      children: [
        {
          path: '',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '管理控制台' }
        },
        {
          path: 'seller-requests',
          name: 'SellerRequests',
          component: () => import('@/views/admin/SellerRequests.vue'),
          meta: { title: '卖家申请管理' }
        },
        {
          path: 'seller-management',
          name: 'SellerManagement',
          component: () => import('@/views/admin/SellerManagement.vue'),
          meta: { title: '卖家管理' }
        },
        {
          path: 'users',
          name: 'UserManagement',
          component: () => import('@/views/admin/UserManagement.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'products',
          name: 'ProductManagement',
          component: () => import('@/views/admin/ProductManagement.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: 'orders',
          name: 'OrderManagement',
          component: () => import('@/views/admin/OrderManagement.vue'),
          meta: { title: '订单管理' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('token')
  const userRole = localStorage.getItem('role') ? parseInt(localStorage.getItem('role') as string) : null
  
  // 检查是否是卖家申请路径或公开路径
  if (to.path === '/apply-seller' || to.path === '/login' || to.path === '/register') {
    // 允许所有用户访问卖家申请页面和登录/注册页面
    return next()
  }
  
  // 检查是否需要登录
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isLoggedIn) {
      console.log('用户未登录，重定向到登录页面')
      return next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      // 检查角色权限
      const hasRequiredRole = to.matched.some(record => {
        const roles = record.meta.roles as number[] | undefined
        return !roles || (userRole !== null && roles.includes(userRole))
      })
      
      if (hasRequiredRole) {
        next()
      } else if (to.path.startsWith('/admin') && userRole !== 2) {
        // 如果尝试访问管理员路由但不是管理员角色
        next('/')
      } else if (to.path.startsWith('/seller') && userRole !== 1) {
        // 如果尝试访问卖家路由但不是卖家角色
        next('/')
      } else {
        next()
      }
    }
  } else {
    // 对于首页特殊处理，不要求登录也可以查看
    if (to.path === '/' || to.path.startsWith('/category/') || to.path.startsWith('/product/') || to.path.startsWith('/search')) {
      return next()
    }
    next()
  }
})

export default router 