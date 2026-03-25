<template>
  <div class="user-manage">
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px">用户管理</h2>

    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="32" :src="row.avatarUrl" icon="UserFilled" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isBanned" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isBanned ? 'danger' : 'success'" size="small">
            {{ row.isBanned ? '封禁' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row)" :disabled="row.role === 'ADMIN'">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllUsersApi, deleteUserApi } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    const res = await getAllUsersApi()
    users.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？此操作不可撤回。`, '确认', { type: 'warning' })
  try {
    await deleteUserApi(row.id)
    ElMessage.success('用户已删除')
    await loadUsers()
  } catch (e) {
    // handled
  }
}

onMounted(() => loadUsers())
</script>
