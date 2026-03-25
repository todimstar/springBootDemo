<template>
  <div class="admin-layout">
    <AppHeader />
    <div class="admin-body">
      <aside class="admin-sidebar">
        <el-menu
          :default-active="activeMenu"
          router
          class="admin-menu"
        >
          <el-menu-item index="/admin">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/posts">
            <el-icon><Document /></el-icon>
            <span>帖子审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Folder /></el-icon>
            <span>分区管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </aside>
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import { DataBoard, Document, Folder, User } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

.admin-body {
  display: flex;
}

.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  min-height: calc(100vh - var(--header-height));
  border-right: 1px solid var(--color-border-light);
}

.admin-menu {
  border-right: none;
}

.admin-main {
  flex: 1;
  padding: 24px;
  min-width: 0;
}
</style>
