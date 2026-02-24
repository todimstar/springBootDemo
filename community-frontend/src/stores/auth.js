// 1. 从 pinia 导入 defineStore，这是创建 store 的核心函数
import { defineStore } from 'pinia'
// 2. 从 vue 导入 ref，我们用它来创建响应式的 state
import { ref } from 'vue'
// 3. 导入 axios 用于API请求
import axios from 'axios'
// 4. 导入 router，以便在登录成功后进行页面跳转
import router from '@/router'
import { computed } from 'vue'

// 5. 调用 defineStore() 来创建一个 store
//    第一个参数 'auth' 是这个 store 的唯一ID，Pinia用它来区分不同的store
//    第二个参数是一个函数，我们需要在这个函数里定义 state, getters, actions
export const useAuthStore = defineStore('auth', () => {
  // --- State ---
  // 6. 定义 token state，初始值为 null。
  //    我们还尝试从浏览器的 localStorage 读取之前保存的token，这样即使用户刷新页面，登录状态也能保持。
  const token = ref(localStorage.getItem('token'))
  // 关键修复：localStorage 只能存字符串，必须用 JSON.parse() 还原成对象
  // 用 try/catch 包裹，防止 localStorage 里的内容格式损坏时直接白屏崩溃
  const user = ref((() => {
    try {
      return JSON.parse(localStorage.getItem('user') || '{}')
    } catch (e) {
      console.warn('[auth store] localStorage 里的 user 数据解析失败，已重置为空对象')
      console.warn('  错误类型:', e.name)        // 错误名称，如 SyntaxError
      console.warn('  错误信息:', e.message)     // 具体描述，如 Unexpected token ...
      console.warn('  原始数据:', localStorage.getItem('user')) // 打出损坏的原始字符串
      return {}
    }
  })())

  // --- Actions ---
  // 7. 定义一个名为 login 的 action。它是一个 async 函数，接收登录所需的参数。
  async function login(usernameOrEmail, password) {
    try {
      console.log('执行了login函数')
      // a. 调用你的后端登录API
      const response = await axios.post('/api/auth/login', {
        usernameOrEmail: usernameOrEmail,
        password: password,
      })

      // b. 从响应中获取JWT Token
      const responseToken = response.data.data.jwtToken

      // c. 更新 state
      token.value = responseToken

      // d. 将 token 存入浏览器的 localStorage，以便持久化
      localStorage.setItem('token', responseToken)

      // 附加：登录成功后，我们通常需要获取当前用户信息
      // 我们可以再发一个请求，或者更好的方式是在登录成功后，后端直接返回用户信息
      // 这里我们先假设登录成功后，需要再请求一次用户信息
      // (在你的后端，你可能需要创建一个 /api/users/me 这样的接口)
      // 为了简化，我们暂时先把用户名存起来
      // 关键修复：直接赋值新对象，而不是 Object.assign（避免操作到字符串引用）
      user.value = { username: response.data.data.username }

      localStorage.setItem('user', JSON.stringify(user.value))
      console.log(localStorage.getItem('user'))

      // e. 登录成功后，跳转到首页或帖子列表页
      await router.push('/')
    } catch (error) {
      // 如果登录失败，打印错误信息
      console.error('登录失败:', error)
      // 在这里你可以添加一些用户提示，比如弹出一个错误提示框
      alert('登录失败，请检查用户名和密码！')
    }
  }

  // 8. 定义一个名为 logout 的 action
  function logout() {
    // a. 清空 state
    token.value = null
    // 关键修复：直接赋值空对象，而不是 Object.assign（避免操作到字符串引用）
    user.value = {}

    // b. 清空 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('user')

    // c. 跳转到登录页
    router.push('/login')
  }

  // --- Getters (可选，但推荐) ---
  // 9. 定义一个计算属性，用于判断用户是否已登录
  const isAuthenticated = computed(() => !!token.value) // 通过 token 是否存在来判断

  // 10. 最后，必须将你需要暴露给外部使用的 state, actions, getters 返回出去
  return { token, user, login, logout, isAuthenticated }
})
