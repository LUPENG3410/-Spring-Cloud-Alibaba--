import axios from 'axios'
import router from '@/router'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 8000,
  headers: { 'Content-Type': 'application/json' }
})

request.interceptors.request.use(config => {
  const isAdmin = config.url?.startsWith('/admin')
  const token = isAdmin ? localStorage.getItem('admin_token') : localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => res,
  err => {
    const status = err.response?.status
    const body = err.response?.data
    const url = err.config?.url || ''

    if (status === 401) {
      const isAdmin = url.startsWith('/admin')
      if (isAdmin) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
      }
    }

    console.warn(`API请求失败 [${status}]:`, JSON.stringify(body || err.message))
    return Promise.reject(err)
  }
)

export default request
