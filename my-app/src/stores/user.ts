import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import { useApi } from '@/api/service'

export const useUserStore = defineStore('user', () => {
  const api = useApi()

  // 从 localStorage 恢复状态
  const savedUser = localStorage.getItem('user')
  const user = ref<User | null>(savedUser ? JSON.parse(savedUser) : null)
  const isLoggedIn = ref(!!localStorage.getItem('token'))
  const loginError = ref('')

  async function login(phone: string, password: string) {
    loginError.value = ''
    try {
      const result = await api.login(phone, password)
      if (result.status === 'disabled') {
        loginError.value = '账号已被禁用，请联系管理员'
        return false
      }
      user.value = result
      isLoggedIn.value = true
      localStorage.setItem('user', JSON.stringify(result))
      return true
    } catch (e: any) {
      loginError.value = e?.response?.data?.message || '登录失败，请检查网络'
      return false
    }
  }

  async function register(name: string, phone: string, password: string) {
    const result = await api.register(name, phone, password)
    localStorage.setItem('token', result.token)
  }

  function logout() {
    user.value = null
    isLoggedIn.value = false
    loginError.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, isLoggedIn, loginError, login, register, logout }
})
