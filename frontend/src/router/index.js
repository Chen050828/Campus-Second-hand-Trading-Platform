import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('../views/ProductDetail.vue') },
  { path: '/cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { auth: true } },
  { path: '/orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { auth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
  { path: '/wallet', name: 'Wallet', component: () => import('../views/Wallet.vue'), meta: { auth: true } },
  // 商家专属页面
  { path: '/merchant', name: 'Merchant', component: () => import('../views/Merchant.vue'), meta: { auth: true, role: 'MERCHANT' } },
  { path: '/merchant/store/:id', name: 'Store', component: () => import('../views/Store.vue') },
  // 管理员专属页面
  { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue'), meta: { auth: true, role: 'ADMIN' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫：未登录跳转登录页，角色不匹配跳转首页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.auth && !token) {
    next('/login')
  } else if (to.meta.role && to.meta.role !== role) {
    next('/')
  } else {
    next()
  }
})

export default router
