import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi } from '@/api/auth'
import router from '@/router'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const userId = computed(() => user.value?.id || null)

  async function login(usernameOrEmail, password) {
    const res = await loginApi({ usernameOrEmail, password })
    const data = res.data
    token.value = data.jwtToken
    // 后端 LoginResponseVO 只返回 username + jwtToken，暂存基本信息
    user.value = { username: data.username, ...(data.id ? { id: data.id } : {}) }
    localStorage.setItem('token', data.jwtToken)
    localStorage.setItem('user', JSON.stringify(user.value))
    ElMessage.success(`欢迎回来，${data.username}`)
    await router.push('/')
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  function updateUserInfo(info) {
    user.value = { ...user.value, ...info }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return { token, user, isAuthenticated, isAdmin, userId, login, logout, updateUserInfo }
})
