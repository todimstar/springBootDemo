<script setup>
import { RouterLink, RouterView } from 'vue-router'
// 引入 storeToRefs，这是一个辅助函数，可以帮助我们从 store 中解构出响应式的数据
// 英文解释: storeToRefs 是一个来自 Pinia 的函数 (function)，
// 它的作用是将一个 store 对象转换成一个包含所有 state、getter 和 action 的普通对象，
// 但这个对象里的每一个属性都是一个 ref。这样我们就可以在不失去响应性的情况下解构 store。
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth.js'

// 获取 auth store 实例
const authStore = useAuthStore()

// 使用 storeToRefs 来解构，确保 isAuthenticated 和 user 保持响应性
const { isAuthenticated, user } = storeToRefs(authStore)
// logout 是一个 action (动作)，可以直接从 store 中解构
const { logout } = authStore
</script>

<template>
  <header>
    <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="60" height="60" />
    <div class="wrapper">
      <nav>
        <!-- 基础导航链接 -->
        <router-link to="/">首页</router-link>
        <router-link to="/posts">帖子列表</router-link>
        <router-link :to="{ name: 'about' }">关于我们</router-link>

        <!-- 未登录时显示 -->
        <router-link v-if="!isAuthenticated" to="/login">登录</router-link>

        <!-- 登录后显示 -->
        <div v-else class="user-info">
          <router-link to="/" class="profile-link">欢迎, {{ user.username }}</router-link>
          <!-- @click.prevent 阻止 a 标签的默认跳转行为 -->
          <a href="#" @click.prevent="logout" class="logout-link">登出</a>
        </div>
<!--        <p>isAuthenticated:{{ isAuthenticated }}</p>-->
      </nav>
    </div>
  </header>

  <RouterView />
</template>
<style scoped>
/*
  `scoped` 属性
  原理: 当 <style> 标签带有 `scoped` 属性时，它的 CSS 只会应用到当前组件的元素上。
  Vue 会为当前组件的 DOM 元素添加一个唯一的自定义属性 (例如 `data-v-f3f3eg9`)，
  然后改写 CSS 选择器，使其只匹配带有这个属性的元素 (例如 `header` 会变成 `header[data-v-f3f3eg9]`)。
  作用: 这是一种 CSS 作用域封装技术，可以防止组件的样式“泄露”到子组件或父组件，
  也避免了全局样式对当前组件的意外影响，让组件样式更加独立和可维护。
*/
header {
  line-height: 1.5;
  max-height: 100vh;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
}

.logo {
  margin-right: 2rem;
}

.wrapper {
  width: 100%;
}

nav {
  width: 100%;
  font-size: 1rem;
  text-align: left;
  display: flex;
  align-items: center;
  gap: 1rem; /* 设置 flex 项目之间的间距 */
}

nav a {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none; /* 去掉下划线 */
  color: var(--color-text);
  transition: background-color 0.3s; /* 添加背景色变化的过渡动画，持续0.3秒 */
}

/*
  `router-link-exact-active` 是 Vue Router 提供的特殊 class (类名)。
  原理: 当一个 <router-link> 对应的路由被精确匹配时 (exact-active)，Vue Router 会自动给这个链接组件添加 `router-link-exact-active` 这个 class。
  作用: 我们可以利用这个 class 来高亮显示当前用户所在的页面对应的导航链接，提升用户体验。
  动手实践: 你可以尝试将 `to="/"` 的链接改为 `to="/home"`，然后访问首页，看看这个 class 是否还会应用到“首页”链接上。
*/
nav a.router-link-exact-active {
  color: var(--color-heading);
  background-color: hsla(160, 100%, 37%, 0.1);
}

nav a:hover {
  background-color: hsla(160, 100%, 37%, 0.05);
}

/*
  `margin-left: auto` 在 Flexbox 布局中的应用
  原理: 在一个 flex 容器中，当一个 flex 项目的边距 (margin) 被设置为 `auto` 时，它会占据所有可用的空间。
  如果设置 `margin-left: auto`，它就会把左边的所有可用空间都“吃掉”，从而将自己推到容器的最右边。
  作用: 这是一个非常实用的技巧，用于将一组元素推到 flex 容器的一端。在这里，我们用它来把用户信息和登出按钮推到导航栏的最右侧。
*/
.user-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logout-link {
  color: #f44336; /* 登出链接用红色突出显示，给用户明确的视觉提示 */
}

.logout-link:hover {
  color: #fff;
  background-color: #f44336;
}
</style>
