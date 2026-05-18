import axios from 'axios'
import { ElMessage } from 'element-plus'

// axios实例，baseURL指向Vite代理（开发时转发到localhost:8088）
const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动附加JWT token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 403) {
      ElMessage.error('权限不足')
    } else if (error.response?.status === 401) {
      // 登录过期，清除token并跳转登录页
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response?.data?.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default api
