<template>
  <div v-if="loading" class="flex-center" style="padding: 80px; justify-content: center">
    <el-icon class="is-loading" :size="32"><Loading /></el-icon>
  </div>

  <div v-else-if="post" class="post-detail-page">
    <!-- Status banner -->
    <el-alert
      v-if="post.status !== 2"
      :title="statusText"
      :type="statusType"
      :closable="false"
      show-icon
      class="mb-16"
    />

    <!-- Post header -->
    <div class="post-header">
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-meta flex-center gap-12 text-secondary">
        <el-tag size="small" effect="plain">{{ post.categoryName || '未分类' }}</el-tag>
        <span>{{ formatDate(post.createTime) }}</span>
        <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
        <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
      </div>
    </div>

    <!-- Tags placeholder -->
    <div class="tags-row mb-16">
      <el-tag type="info" size="small" effect="plain">标签功能开发中</el-tag>
    </div>

    <!-- Author card -->
    <div class="author-card card flex-center gap-12 mb-16">
      <el-avatar :size="40" icon="UserFilled" />
      <div>
        <div class="author-name">作者</div>
        <div class="text-secondary">作者详细信息接口完善中</div>
      </div>
      <div style="margin-left: auto">
        <el-button v-if="isAuthenticated" size="small" @click="$router.push(`/edit-post/${post.id}`)">编辑</el-button>
        <el-button v-if="isAuthenticated" size="small" type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <!-- Content -->
    <div class="card mb-20">
      <MarkdownRenderer :content="post.content" />
    </div>

    <!-- Actions placeholder -->
    <div class="actions-bar flex-center gap-16 mb-20">
      <el-button disabled>
        <el-icon><Star /></el-icon> 点赞 (开发中)
      </el-button>
      <el-button disabled>
        <el-icon><Collection /></el-icon> 收藏 (开发中)
      </el-button>
    </div>

    <!-- Comments -->
    <div class="card">
      <CommentSection :post-id="post.id" />
    </div>
  </div>

  <div v-else class="placeholder-section" style="margin-top: 60px">
    帖子不存在或已被删除
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { getPostApi, deletePostApi } from '@/api/posts'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import CommentSection from '@/components/CommentSection.vue'
import { View, ChatDotRound, Star, Collection, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { isAuthenticated } = storeToRefs(authStore)

const post = ref(null)
const loading = ref(true)

const statusText = computed(() => {
  const s = post.value?.status
  const map = { 0: '草稿 - 仅自己可见', 1: '待审核 - 等待管理员审核', 3: '已拒绝', 4: '已删除' }
  return map[s] || ''
})

const statusType = computed(() => {
  const s = post.value?.status
  const map = { 0: 'info', 1: 'warning', 3: 'error', 4: 'error' }
  return map[s] || 'info'
})

function formatDate(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

async function handleDelete() {
  await ElMessageBox.confirm('确定删除这篇帖子吗？', '确认删除', { type: 'warning' })
  try {
    await deletePostApi(post.value.id)
    ElMessage.success('帖子已删除')
    router.push('/')
  } catch (e) {
    // handled in interceptor
  }
}

onMounted(async () => {
  try {
    const res = await getPostApi(route.params.id)
    post.value = res.data
  } catch (e) {
    post.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.post-detail-page {
  max-width: 800px;
  margin: 0 auto;
}

.post-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
}

.post-meta span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.tags-row {
  display: flex;
  gap: 6px;
}

.author-card {
  padding: 12px 16px;
}

.author-name {
  font-weight: 600;
  font-size: 15px;
}

.actions-bar {
  justify-content: center;
}
</style>
