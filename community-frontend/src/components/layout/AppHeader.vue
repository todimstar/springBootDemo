<template>
  <div class="app-header">
    <div class="header-inner">
      <div class="header-left flex-center gap-16">
        <router-link to="/" class="logo-link flex-center gap-8">
          <el-icon :size="24" color="#409eff"><Connection /></el-icon>
          <span class="logo-text">TechForum</span>
        </router-link>
        <nav class="nav-links flex-center gap-8">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link v-if="isAuthenticated" to="/create-post" class="nav-item">创作</router-link>
        </nav>
      </div>

      <div class="header-center">
        <el-input
          placeholder="搜索功能开发中..."
          disabled
          prefix-icon="Search"
          size="default"
          style="width: 300px"
        />
      </div>

      <div class="header-right flex-center gap-12">
        <template v-if="isAuthenticated">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-trigger flex-center gap-8">
              <el-avatar :size="32" :src="user?.avatarUrl" icon="UserFilled" />
              <span>{{ user?.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="me">个人中心</el-dropdown-item>
                <el-dropdown-item command="my-posts">我的帖子</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin" divided>管理后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <span style="color: #f56c6c">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录 / 注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { Connection, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const { isAuthenticated, user, isAdmin } = storeToRefs(authStore)

function handleCommand(cmd) {
  switch (cmd) {
    case 'me': router.push('/me'); break
    case 'my-posts': router.push('/me'); break
    case 'admin': router.push('/admin'); break
    case 'logout': authStore.logout(); break
  }
}
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  background: #fff;
  border-bottom: 1px solid var(--color-border-light);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-link {
  text-decoration: none;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.nav-item {
  padding: 6px 14px;
  border-radius: 6px;
  color: #606266;
  font-size: 14px;
  transition: all 0.2s;
}

.nav-item:hover,
.nav-item.router-link-exact-active {
  color: #409eff;
  background: #ecf5ff;
}

.user-trigger {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-trigger:hover {
  background: #f5f7fa;
}
</style>
