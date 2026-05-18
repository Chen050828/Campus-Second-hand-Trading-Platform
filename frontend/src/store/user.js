import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

// 全局用户状态管理：登录态、角色判断、token持久化
export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  // token和role持久化到localStorage，刷新页面不丢失登录态
  const token = ref(localStorage.getItem('token') || '')
  const role = ref(localStorage.getItem('role') || '')

  // 派生状态：根据token是否存在判断登录，根据role判断权限
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isMerchant = computed(() => role.value === 'MERCHANT')
  const isUser = computed(() => role.value === 'USER')

  async function login(username, password, captcha, captchaKey) {
    const res = await api.post('/auth/login', { username, password, captcha, captchaKey })
    if (res.data.code === 200) {
      const d = res.data.data
      token.value = d.token
      role.value = d.role
      user.value = { id: d.userId, username: d.username, name: d.name, role: d.role }
      localStorage.setItem('token', d.token)
      localStorage.setItem('role', d.role)
      localStorage.setItem('userId', d.userId)
      return { success: true }
    }
    return { success: false, message: res.data.message }
  }

  function logout() {
    token.value = ''
    role.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
  }

  // 拉取完整用户信息（用于个人中心展示）
  async function fetchProfile() {
    try {
      const res = await api.get('/user/profile')
      if (res.data.code === 200) {
        user.value = res.data.data
      }
    } catch (e) { /* 静默失败，用户信息加载失败不影响基础功能 */ }
  }

  return { user, token, role, isLoggedIn, isAdmin, isMerchant, isUser, login, logout, fetchProfile }
})
